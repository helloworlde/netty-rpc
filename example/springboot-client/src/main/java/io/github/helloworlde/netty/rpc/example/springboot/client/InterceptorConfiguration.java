package io.github.helloworlde.netty.rpc.example.springboot.client;

import io.github.helloworlde.netty.opentelemetry.metrics.ClientMetricsInterceptor;
import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterceptorConfiguration {

    @Bean
    public CustomClientInterceptor clientInterceptor() {
        return new CustomClientInterceptor();
    }

    @Bean
    public ClientMetricsInterceptor metricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ClientMetricsInterceptor(collectorRegistry);
    }
}
