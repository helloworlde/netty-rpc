package io.github.helloworlde.netty.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class ZipkinConfiguration {

    private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";

    public static OpenTelemetry initOpenTelemetry(String serviceName, String ip, int port) {
        String httpUrl = String.format("http://%s:%d", ip, port);

        ZipkinSpanExporter zipkinSpanExporter = ZipkinSpanExporter.builder()
                                                                  .setEndpoint(httpUrl + ENDPOINT_V2_SPANS)
                                                                  .build();

        Resource resource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                                                               .addSpanProcessor(SimpleSpanProcessor.create(zipkinSpanExporter))
                                                               .setResource(Resource.getDefault().merge(resource))
                                                               .build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                                                         .setTracerProvider(sdkTracerProvider)
                                                         .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                                                         .build();

        Runtime.getRuntime().addShutdownHook(new Thread(sdkTracerProvider::shutdown));


        return openTelemetry;

    }
}
