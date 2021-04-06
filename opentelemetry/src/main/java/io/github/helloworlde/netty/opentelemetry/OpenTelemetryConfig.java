package io.github.helloworlde.netty.opentelemetry;

import io.github.helloworlde.netty.opentelemetry.trace.config.ExporterType;
import io.github.helloworlde.netty.opentelemetry.trace.config.JaegerConfiguration;
import io.github.helloworlde.netty.opentelemetry.trace.config.LoggingConfiguration;
import io.github.helloworlde.netty.opentelemetry.trace.config.ZipkinConfiguration;
import io.opentelemetry.api.OpenTelemetry;

public class OpenTelemetryConfig {

    public static OpenTelemetry getOpenTelemetry(ExporterType exporter,
                                                 String serviceName,
                                                 String host,
                                                 Integer port,
                                                 int sampleRatio) {
        OpenTelemetry openTelemetry;
        switch (exporter) {
            case jaeger:
                openTelemetry = JaegerConfiguration.initOpenTelemetry(serviceName, host, port, sampleRatio);
                break;
            case zipkin:
                openTelemetry = ZipkinConfiguration.initOpenTelemetry(serviceName, host, port, sampleRatio);
                break;
            default:
                openTelemetry = LoggingConfiguration.initOpenTelemetry(serviceName);
        }
        return openTelemetry;
    }

    public static OpenTelemetry getOpenTelemetry(ExporterType exporter,
                                                 String serviceName,
                                                 String host,
                                                 Integer port) {
        return getOpenTelemetry(exporter, serviceName, host, port, 1);
    }

}
