package io.github.helloworlde.netty.rpc.example.springboot.server;


import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

// @Component
public class ConsulConfiguration {

    @Value("${consul.host}")
    private String consulHost;

    @Value("${consul.port}")
    private Integer consulPort;

    @Bean
    public ConsulRegistry consulRegistry() {
        return new ConsulRegistry(consulHost, consulPort);
    }

}
