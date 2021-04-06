package io.github.helloworlde.netty.rpc.example.springboot.server.interceptor;

import io.github.helloworlde.netty.opentelemetry.metrics.ServerMetricsInterceptor;
import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterceptorConfiguration {

    @Bean
    public CustomServerInterceptor clientInterceptor() {
        return new CustomServerInterceptor();
    }

    @Bean
    public ServerMetricsInterceptor serverMetricsInterceptor(CollectorRegistry collectorRegistry) {
        return new ServerMetricsInterceptor(collectorRegistry);
    }
}
