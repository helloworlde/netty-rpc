package io.github.helloworlde.netty.opentelemetry.metrics;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ClientMetricsInterceptor implements ClientInterceptor {

    private CollectorRegistry collectorRegistry;

    private final AtomicLong requestCounter = new AtomicLong();

    private final LongCounter requestCounterLong;

    private final AggregatorHandle<Object> requestTimeHandler;

    public ClientMetricsInterceptor(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(collectorRegistry);
        Meter meter = metricsConfiguration.getMeter();

        meter.longValueObserverBuilder("netty.rpc.request.count")
             .setDescription("请求个数")
             .setUnit("request")
             .setUpdater(result -> result.observe(requestCounter.get(), Labels.of("role", "client")))
             .build();

        requestCounterLong = meter.longCounterBuilder("netty.rpc.request.sum")
                                  .setDescription("总请求个数")
                                  .setUnit("个")
                                  .build();
        final Aggregator<Object> objectAggregator = AggregatorFactory.count(AggregationTemporality.CUMULATIVE)
                                                                     .create(Resource.getDefault(),
                                                                             InstrumentationLibraryInfo.empty(),
                                                                             InstrumentDescriptor.create(
                                                                                     "netty.rpc.request.time",
                                                                                     "请求耗费时间",
                                                                                     "ms",
                                                                                     InstrumentType.VALUE_RECORDER,
                                                                                     InstrumentValueType.DOUBLE
                                                                             ));


        requestTimeHandler = objectAggregator.createHandle();
    }


    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        requestCounter.incrementAndGet();

        Instant startTime = Instant.now();

        log.info("当前请求个数: {}", requestCounter.get());
        Object result;
        try {
            result = next.call(request, callOptions);
            requestCounterLong.add(1L, Labels.of("result", "success"));
        } catch (Exception e) {
            requestCounterLong.add(1L, Labels.of("result", "error"));
            throw e;
        } finally {
            long costTime = Duration.between(Instant.now(), startTime).toMillis();
            requestTimeHandler.recordLong(costTime);
        }
        return result;
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 2;
    }
}
