package io.github.helloworlde.netty.rpc.starter.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyRpcServerAutoConfiguration {

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory() {
        return new NettyRpcServiceFactory();
    }

    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }
}
