package io.github.helloworlde.netty.rpc.example.springboot.client;


import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

// @Component
public class ConsulConfiguration {

    @Value("${consul.host}")
    private String consulHost;

    @Value("${consul.port}")
    private Integer consulPort;

    @Bean
    public ConsulNameResolver consulNameResolver() {
        return new ConsulNameResolver(consulHost, consulPort);
    }

}
