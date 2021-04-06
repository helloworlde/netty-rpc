package io.github.helloworlde.netty.rpc.example.springboot.client.interceptor;

import io.github.helloworlde.netty.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.opentelemetry.metrics.ClientMetricsInterceptor;
import io.github.helloworlde.netty.opentelemetry.trace.client.ClientTraceInterceptor;
import io.github.helloworlde.netty.opentelemetry.trace.config.ExporterEnum;
import io.opentelemetry.api.OpenTelemetry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterceptorConfiguration {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${opentelemetry.exporter.name}")
    private ExporterEnum exporter;

    @Value("${opentelemetry.exporter.address}")
    private String exporterAddress;

    @Value("${opentelemetry.exporter.port}")
    private Integer exporterPort;

    @Bean
    public CustomClientInterceptor clientInterceptor() {
        return new CustomClientInterceptor();
    }

    @Bean
    public ClientMetricsInterceptor metricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ClientMetricsInterceptor(collectorRegistry);
    }

    @Bean
    public ClientTraceInterceptor clientTraceInterceptor() {
        OpenTelemetry openTelemetry = OpenTelemetryConfig.getOpenTelemetry(exporter, applicationName, exporterAddress, exporterPort);
        return new ClientTraceInterceptor(openTelemetry);
    }
}
