package io.github.helloworlde.netty.rpc.example.springboot.server.interceptor;

import io.github.helloworlde.netty.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.opentelemetry.metrics.ServerMetricsInterceptor;
import io.github.helloworlde.netty.opentelemetry.trace.config.ExporterEnum;
import io.github.helloworlde.netty.opentelemetry.trace.server.ServerTraceInterceptor;
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
    public CustomServerInterceptor clientInterceptor() {
        return new CustomServerInterceptor();
    }

    @Bean
    public ServerMetricsInterceptor serverMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ServerMetricsInterceptor(collectorRegistry);
    }

    @Bean
    public ServerTraceInterceptor clientTraceInterceptor() {
        OpenTelemetry openTelemetry = OpenTelemetryConfig.getOpenTelemetry(exporter, applicationName, exporterAddress, exporterPort);
        return new ServerTraceInterceptor(openTelemetry);
    }
}
