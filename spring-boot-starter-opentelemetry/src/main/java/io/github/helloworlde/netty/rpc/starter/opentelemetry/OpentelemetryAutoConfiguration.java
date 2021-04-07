package io.github.helloworlde.netty.rpc.starter.opentelemetry;

import io.github.helloworlde.netty.rpc.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.rpc.opentelemetry.metrics.ClientMetricsInterceptor;
import io.github.helloworlde.netty.rpc.opentelemetry.metrics.ServerMetricsInterceptor;
import io.github.helloworlde.netty.rpc.opentelemetry.trace.client.ClientTraceInterceptor;
import io.github.helloworlde.netty.rpc.opentelemetry.trace.server.ServerTraceInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(OpenTelemetryProperties.class)
public class OpentelemetryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(OpenTelemetryProperties properties) {
        return OpenTelemetryConfig.getOpenTelemetry(properties.getTrace().getExporter().getName(),
                properties.getServiceName(),
                properties.getTrace().getExporter().getAddress(),
                properties.getTrace().getExporter().getPort(),
                properties.getTrace().getSampleRatio());
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.opentelemetry.trace.enabled", matchIfMissing = true)
    public ClientTraceInterceptor clientTraceInterceptor(OpenTelemetry openTelemetry) {
        return new ClientTraceInterceptor(openTelemetry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.opentelemetry.metrics.enabled", matchIfMissing = true)
    public ClientMetricsInterceptor clientMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ClientMetricsInterceptor(collectorRegistry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.opentelemetry.trace.enabled", matchIfMissing = true)
    public ServerTraceInterceptor serverTraceInterceptor(OpenTelemetry openTelemetry) {
        return new ServerTraceInterceptor(openTelemetry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.opentelemetry.metrics.enabled", matchIfMissing = true)
    public ServerMetricsInterceptor serverMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ServerMetricsInterceptor(collectorRegistry);
    }
}
