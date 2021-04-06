package io.github.helloworlde.netty.opentelemetry.metrics;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongValueObserver;
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
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ClientMetricsInterceptor implements ClientInterceptor {

    private CollectorRegistry collectorRegistry;

    private final AtomicLong requestCounter = new AtomicLong();

    private final LongCounter requestCounterLong;

    private final AggregatorHandle<Object> requestTimeHandler;

    private DoubleHistogramPointData doubleHistogram;
    private MetricData timeHistogram;


    static final String DIMENSION_API_NAME = "apiName";
    static final String DIMENSION_STATUS_CODE = "statusCode";

    static String API_COUNTER_METRIC = "apiBytesSent";
    static String API_LATENCY_METRIC = "latency";
    static String API_SUM_METRIC = "totalApiBytesSent";
    static String API_LAST_LATENCY_METRIC = "lastLatency";
    static String API_UP_DOWN_COUNTER_METRIC = "queueSizeChange";
    static String API_UP_DOWN_SUM_METRIC = "actualQueueSize";

    String apiNameValue = "";
    String statusCodeValue = "";

    long apiLastLatency = 1;
    LongValueObserver apiLastLatencyObserver;

    double costTime = 0;
    DoubleValueRecorder costRecorder;

    final Aggregator<Object> objectAggregator;

    int counter = 0;


    public ClientMetricsInterceptor(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(collectorRegistry);
        Meter meter = metricsConfiguration.getMeter();

        meter.longValueObserverBuilder("netty.rpc.request.count")
             .setDescription("请求个数")
             .setUnit("request")
             .setUpdater(result -> result.observe(requestCounter.get(), Labels.of("role", "client")))
             .build();

        requestCounterLong = meter.longCounterBuilder("netty.rpc.request")
                                  .setDescription("总请求个数")
                                  .setUnit("个")
                                  .build();

        AggregatorFactory aggregatorFactory = metricsConfiguration.getAggregatorFactory();

        objectAggregator = aggregatorFactory.create(Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                InstrumentDescriptor.create(
                        "netty.rpc.request.time",
                        "请求耗费时间",
                        "ms",
                        InstrumentType.VALUE_RECORDER,
                        InstrumentValueType.DOUBLE
                ));

        requestTimeHandler = objectAggregator.createHandle();

        costRecorder = meter.doubleValueRecorderBuilder("netty.rpc.request.cost")
                            .setDescription("时间哈哈哈哈")
                            .setUnit("ms")
                            .build();

        meter.doubleSumObserverBuilder("netty.rpc.request.cost.sum")
             .setDescription("时间统计")
             .setUnit("ms")
             .setUpdater(doubleResult -> {
                 doubleResult.observe(costTime, Labels.of("time", "request"));
             });

        //
        // MetricData doubleHistogram = MetricData.createDoubleHistogram();


        DoubleHistogramPointData doubleHistogramPointData = DoubleHistogramPointData.create(
                0,
                100,
                Labels.of("role", "client"),
                100,
                Arrays.asList(0.5d, 0.9d, 1d),
                Arrays.asList(5L, 9L, 99L, 100L)
        );

        final DoubleHistogramData doubleHistogramData = DoubleHistogramData.create(AggregationTemporality.CUMULATIVE, Arrays.asList(doubleHistogramPointData));


        timeHistogram = MetricData.createDoubleHistogram(
                Resource.getDefault(),
                InstrumentationLibraryInfo.empty(),
                "netty.rpc.request.time",
                "请求耗时",
                "ms",
                doubleHistogramData
        );


        apiLastLatencyObserver = meter.longValueObserverBuilder("netty.rpc.request.time")
                                      .setDescription("请求花费的时间啊啊啊")
                                      .setUnit("ms")
                                      .setUpdater(longResult -> {
                                          longResult.observe(apiLastLatency,
                                                  Labels.of(DIMENSION_API_NAME,
                                                          apiNameValue,
                                                          DIMENSION_STATUS_CODE,
                                                          statusCodeValue));
                                      })
                                      .build();

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
            long costTime = Duration.between(startTime, Instant.now()).toMillis();
            apiLastLatency = costTime;
            this.costTime = costTime;
            log.info("花费的时间: {}", costTime);
            requestTimeHandler.recordDouble(costTime);
            costRecorder.record(costTime);
            counter++;
            if (counter > 99) {
                MetricData metricData = objectAggregator.toMetricData(
                        Collections.singletonMap(Labels.empty(), requestTimeHandler.accumulateThenReset()),
                        0,
                        10,
                        100
                );

                // Object o = requestTimeHandler.accumulateThenReset();


                log.info("统计结果: {}", metricData);
            }
        }
        return result;
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 2;
    }
}
