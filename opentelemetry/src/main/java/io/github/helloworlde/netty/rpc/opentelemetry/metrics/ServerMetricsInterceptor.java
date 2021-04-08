package io.github.helloworlde.netty.rpc.opentelemetry.metrics;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
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

    private final LongCounter responseCountCounter;

    private final DoubleValueRecorder responseTimeRecorder;

    public ServerMetricsInterceptor(CollectorRegistry collectorRegistry) {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(collectorRegistry);
        Meter meter = metricsConfiguration.getMeter();

        responseCountCounter = meter.longCounterBuilder("netty.rpc.response.count")
                                    .setDescription("响应数量")
                                    .build();

        responseTimeRecorder = meter.doubleValueRecorderBuilder("netty.rpc.response.duration")
                                    .setDescription("响应时间")
                                    .setUnit("ms")
                                    .build();
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ServerCall next) throws Exception {
        Instant startTime = Instant.now();

        String serviceName = request.getServiceName();
        String methodName = request.getMethodName();

        Object result;
        try {
            result = next.call(request, callOptions);
            recordResponseCount(serviceName, methodName, "SUCCESS");
        } catch (Exception e) {
            recordResponseCount(serviceName, methodName, "ERROR");
            throw e;
        } finally {
            long costTime = Duration.between(startTime, Instant.now()).toMillis();
            recordResponseTime(serviceName, methodName, costTime);
        }
        return result;
    }


    private void recordResponseTime(String serviceName, String methodName, long costTime) {
        Labels labels = Labels.builder()
                              .put("service_name", serviceName)
                              .put("method_name", methodName)
                              .build();
        responseTimeRecorder.record(costTime, labels);
    }

    private void recordResponseCount(String serviceName, String methodName, String outcome) {
        Labels labels = Labels.builder()
                              .put("service_name", serviceName)
                              .put("method_name", methodName)
                              .put("outcome", outcome)
                              .build();
        responseCountCounter.add(1L, labels);
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 2;
    }
}
