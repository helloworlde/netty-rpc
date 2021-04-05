package io.github.helloworlde.netty.opentelemetry;

import io.github.helloworlde.netty.opentelemetry.trace.config.ExporterEnum;
import io.github.helloworlde.netty.opentelemetry.trace.config.JaegerConfiguration;
import io.github.helloworlde.netty.opentelemetry.trace.config.LoggingConfiguration;
import io.github.helloworlde.netty.opentelemetry.trace.config.ZipkinConfiguration;
import io.opentelemetry.api.OpenTelemetry;

public class OpenTelemetryConfig {

    public static OpenTelemetry getOpenTelemetry(ExporterEnum exporter,
                                                 String serviceName,
                                                 String host,
                                                 Integer port) {
        OpenTelemetry openTelemetry;
        switch (exporter) {
            case Jaeger:
                openTelemetry = JaegerConfiguration.initOpenTelemetry(serviceName, host, port);
                break;
            case Zipkin:
                openTelemetry = ZipkinConfiguration.initOpenTelemetry(serviceName, host, port);
                break;
            default:
                openTelemetry = LoggingConfiguration.initOpenTelemetry(serviceName);
        }
        return openTelemetry;
    }

}
