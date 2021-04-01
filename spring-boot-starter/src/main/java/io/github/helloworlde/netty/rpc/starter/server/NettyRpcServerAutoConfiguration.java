package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ServerProperties.class)
@ConditionalOnBean(annotation = NettyRpcService.class, value = Object.class)
public class NettyRpcServerAutoConfiguration {

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory(InetUtils inetUtils, ServerProperties properties) {
        return new NettyRpcServiceFactory(inetUtils, properties);
    }

    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }
}
