package io.github.helloworlde.netty.rpc.example.springboot.server.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InterceptorConfiguration {

    @Bean
    public CustomServerInterceptor clientInterceptor() {
        return new CustomServerInterceptor();
    }

}
