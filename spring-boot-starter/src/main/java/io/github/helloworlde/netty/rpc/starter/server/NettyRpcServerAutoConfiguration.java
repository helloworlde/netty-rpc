package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.starter.annotation.ConditionalOnNettyRpcServerEnabled;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ServerProperties.class)
@ConditionalOnNettyRpcServerEnabled
public class NettyRpcServerAutoConfiguration {

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory(ApplicationContext context,
                                                         InetUtils inetUtils,
                                                         ServerProperties properties) {
        return new NettyRpcServiceFactory(context, inetUtils, properties);
    }

    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }
}
