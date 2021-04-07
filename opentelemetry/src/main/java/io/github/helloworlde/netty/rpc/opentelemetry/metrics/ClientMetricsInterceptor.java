package io.github.helloworlde.netty.rpc.opentelemetry.metrics;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
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
public class ClientMetricsInterceptor implements ClientInterceptor {

    private final LongCounter requestCountCounter;

    private final DoubleValueRecorder requestTimeRecorder;

    public ClientMetricsInterceptor(CollectorRegistry collectorRegistry) {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration(collectorRegistry);
        Meter meter = metricsConfiguration.getMeter();

        // 创建请求数量统计
        requestCountCounter = meter.longCounterBuilder("netty.rpc.request.count")
                                   .setDescription("请求数量")
                                   .build();

        // 创建请求时间统计
        requestTimeRecorder = meter.doubleValueRecorderBuilder("netty.rpc.request.duration")
                                   .setDescription("请求时间")
                                   .setUnit("ms")
                                   .build();
    }


    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        Instant startTime = Instant.now();

        String serviceName = request.getServiceName();
        String methodName = request.getMethodName();

        Object result;
        try {
            result = next.call(request, callOptions);
            // 统计请求数量
            recordRequestCount(serviceName, methodName, "SUCCESS");
        } catch (Exception e) {
            recordRequestCount(serviceName, methodName, "ERROR");
            throw e;
        } finally {
            // 统计请求时间
            long costTime = Duration.between(startTime, Instant.now()).toMillis();
            recordRequestTime(serviceName, methodName, costTime);
        }
        return result;
    }

    private void recordRequestTime(String serviceName, String methodName, long costTime) {
        // 指定 Label，添加到统计中
        Labels labels = Labels.builder()
                              .put("service_name", serviceName)
                              .put("method_name", methodName)
                              .build();
        requestTimeRecorder.record(costTime, labels);
    }

    private void recordRequestCount(String serviceName, String methodName, String outcome) {
        // 指定 Label，添加到统计中
        Labels labels = Labels.builder()
                              .put("service_name", serviceName)
                              .put("method_name", methodName)
                              .put("outcome", outcome)
                              .build();
        requestCountCounter.add(1L, labels);
    }

    @Override
    public Integer getOrder() {
        return Integer.MAX_VALUE - 2;
    }
}
