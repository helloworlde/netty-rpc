package io.github.helloworlde.netty.opentelemetry;

import io.opentelemetry.api.OpenTelemetry;

public class OpenTelemetryConfig {

    public static OpenTelemetry getOpenTelemetry(String serviceName, String host, Integer port) {
        return JaegerConfiguration.initOpenTelemetry(serviceName, host, port);
    }

}
