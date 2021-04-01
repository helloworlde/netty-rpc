package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcClient;
import io.github.helloworlde.netty.rpc.starter.common.TestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NettyRpcStarterClient {

    @NettyRpcClient(value = "netty-rpc-test-server")
    private TestService testService;

    public static void main(String[] args) {
        SpringApplication.run(NettyRpcStarterClient.class);
    }

    @Bean
    public Registry registry() {
        return new ConsulRegistry("127.0.0.1", 8500);
    }

    @Bean
    public NameResolver nameResolver() {
        return new ConsulNameResolver("127.0.0.1", 8500);
    }

}