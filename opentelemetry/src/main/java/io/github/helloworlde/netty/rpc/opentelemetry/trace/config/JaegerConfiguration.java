package io.github.helloworlde.netty.rpc.opentelemetry.trace.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import java.util.concurrent.TimeUnit;

public class JaegerConfiguration {

    public static OpenTelemetry initOpenTelemetry(String serviceName, String jaegerHost, Integer jaegerPort, int sampleRatio) {
        // 创建用于连接 Jaeger 的 gRPC Channel
        ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress(jaegerHost, jaegerPort).usePlaintext().build();

        // 导出到 Jaeger
        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                                                                      .setChannel(jaegerChannel)
                                                                      .setTimeout(30, TimeUnit.SECONDS)
                                                                      .build();

        Resource serviceNameResource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        // 构建 TraceProvider
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                                                               // 采样率
                                                               .setSampler(Sampler.traceIdRatioBased(sampleRatio))
                                                               // Span 批量处理
                                                               .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
                                                               // 上报的属性
                                                               .setResource(Resource.getDefault().merge(serviceNameResource))
                                                               .build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                                                         .setTracerProvider(sdkTracerProvider)
                                                         // Trace 传播
                                                         .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                                                         .build();

        Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));

        return openTelemetry;
    }


}
