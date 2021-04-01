package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.registry.NoopRegistry;
import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.starter.annotation.ConditionalOnNettyRpcServerEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
    @ConditionalOnMissingBean(Registry.class)
    @ConditionalOnProperty(name = "netty.rpc.server.registry.enabled", matchIfMissing = true)
    public Registry registry() {
        return new NoopRegistry();
    }

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory(ApplicationContext context,
                                                         InetUtils inetUtils,
                                                         Registry registry,
                                                         ServerProperties properties) {
        return new NettyRpcServiceFactory(context, inetUtils, registry, properties);
    }

    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }

}
