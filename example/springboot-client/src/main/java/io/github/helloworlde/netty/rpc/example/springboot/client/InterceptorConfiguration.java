package io.github.helloworlde.netty.rpc.example.springboot.client;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterceptorConfiguration {

    @Bean
    public CustomClientInterceptor clientInterceptor() {
        return new CustomClientInterceptor();
    }
}
