package io.github.helloworlde.netty.rpc.starter.opentelemetry;

import io.github.helloworlde.netty.opentelemetry.OpenTelemetryConfig;
import io.github.helloworlde.netty.opentelemetry.metrics.ClientMetricsInterceptor;
import io.github.helloworlde.netty.opentelemetry.metrics.ServerMetricsInterceptor;
import io.github.helloworlde.netty.opentelemetry.trace.client.ClientTraceInterceptor;
import io.github.helloworlde.netty.opentelemetry.trace.server.ServerTraceInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(OpenTelemetryProperties.class)
@ConditionalOnProperty(value = "netty.rpc.opentelemetry.enabled", matchIfMissing = true)
public class OpentelemetryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(OpenTelemetryProperties properties) {
        return OpenTelemetryConfig.getOpenTelemetry(properties.getExporter().getName(),
                properties.getServiceName(),
                properties.getExporter().getAddress(),
                properties.getExporter().getPort(),
                properties.getSampleRatio());
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.client.enabled", matchIfMissing = true)
    public ClientTraceInterceptor clientTraceInterceptor(OpenTelemetry openTelemetry) {
        return new ClientTraceInterceptor(openTelemetry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.client.enabled", matchIfMissing = true)
    public ClientMetricsInterceptor clientMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ClientMetricsInterceptor(collectorRegistry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.server.enabled", matchIfMissing = true)
    public ServerTraceInterceptor serverTraceInterceptor(OpenTelemetry openTelemetry) {
        return new ServerTraceInterceptor(openTelemetry);
    }

    @Bean
    @ConditionalOnProperty(value = "netty.rpc.server.enabled", matchIfMissing = true)
    public ServerMetricsInterceptor serverMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ServerMetricsInterceptor(collectorRegistry);
    }
}
