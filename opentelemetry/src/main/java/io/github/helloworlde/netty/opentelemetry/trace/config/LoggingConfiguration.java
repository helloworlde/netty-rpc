package io.github.helloworlde.netty.opentelemetry.trace.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class LoggingConfiguration {

    public static OpenTelemetry initOpenTelemetry(String serviceName) {

        Resource resource = Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName));

        LoggingSpanExporter exporter = new LoggingSpanExporter();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                                                               .addSpanProcessor(SimpleSpanProcessor.create(exporter))
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
