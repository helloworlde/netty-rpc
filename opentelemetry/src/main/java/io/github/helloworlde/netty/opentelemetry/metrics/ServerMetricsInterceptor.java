package io.github.helloworlde.netty.opentelemetry.metrics;

import io.github.helloworlde.netty.rpc.interceptor.Metadata;
import io.github.helloworlde.netty.rpc.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.interceptor.ServerInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

@Slf4j
public class ServerMetricsInterceptor implements ServerInterceptor {

    private final LongCounter requestCountCounter;

    private final DoubleValueRecorder requestTimeRecorder;

    public ServerMetricsInterceptor(CollectorRegistry collectorRegistry) {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(collectorRegistry);
        Meter meter = metricsConfiguration.getMeter();

        requestCountCounter = meter.longCounterBuilder("netty.rpc.response.count")
                                   .setDescription("请求数量")
                                   .build();

        requestTimeRecorder = meter.doubleValueRecorderBuilder("netty.rpc.response.duration")
                                   .setDescription("请求时间")
                                   .setUnit("ms")
                                   .build();
    }

    @Override
    public Object interceptorCall(Request request, Metadata metadata, ServerCall next) throws Exception {
        Instant startTime = Instant.now();

        String serviceName = request.getServiceName();
        String methodName = request.getMethodName();

        Object result;
        try {
            result = next.call(request, metadata);
            recordRequestCount(serviceName, methodName, "success");
        } catch (Exception e) {
            recordRequestCount(serviceName, methodName, "error");
            throw e;
        } finally {
            recordRequestCount(serviceName, methodName, "sum");

            long costTime = Duration.between(startTime, Instant.now()).toMillis();
            requestTimeRecorder.record(costTime);
        }
        return result;
    }


    private void recordRequestCount(String serviceName, String methodName, String type) {
        Labels labels = Labels.builder()
                              .put("service_name", serviceName)
                              .put("method_name", methodName)
                              .put("type", type)
                              .build();
        requestCountCounter.add(1L, labels);
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 2;
    }
}
