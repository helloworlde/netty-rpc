package io.github.helloworlde.netty.rpc.starter.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyRpcClientAutoConfiguration {

    @Bean
    public NettyRpcClientFactory rpcClientFactory() {
        return new NettyRpcClientFactory();
    }

    @Bean
    public NettyRpcServiceProxyProcessor rpcClientProcessor(NettyRpcClientFactory clientFactory) {
        return new NettyRpcServiceProxyProcessor(clientFactory);
    }

    @Bean
    public NettyRpcClientSmartLifecycle clientSmartLifecycle(ApplicationContext context) {
        return new NettyRpcClientSmartLifecycle(context);
    }
}
