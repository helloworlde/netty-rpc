package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class NettyRpcServerAutoConfiguration {

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory(Environment environment) {
        return new NettyRpcServiceFactory(environment);
    }

    @ConditionalOnBean(annotation = NettyRpcService.class, value = Object.class)
    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }
}
