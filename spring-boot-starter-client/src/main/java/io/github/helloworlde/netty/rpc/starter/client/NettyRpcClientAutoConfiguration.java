package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.nameresovler.FixedAddressNameResolver;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.registry.NoopRegistry;
import io.github.helloworlde.netty.rpc.registry.Registry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@ConditionalOnNettyRpcClientEnabled
public class NettyRpcClientAutoConfiguration {

    @Bean
    public NettyRpcClientFactory rpcClientFactory(ClientProperties clientProperties,
                                                  @Nullable Registry registry,
                                                  @Nullable NameResolver nameResolver,
                                                  @Nullable ClientInterceptor[] interceptors) {
        return new NettyRpcClientFactory(clientProperties, registry, nameResolver, interceptors);
    }

    @Bean
    public NettyRpcServiceProxyProcessor rpcClientProcessor(NettyRpcClientFactory clientFactory) {
        return new NettyRpcServiceProxyProcessor(clientFactory);
    }

    @Bean
    public NettyRpcClientSmartLifecycle clientSmartLifecycle(ApplicationContext context) {
        return new NettyRpcClientSmartLifecycle(context);
    }

    @Bean
    @ConditionalOnMissingBean(NameResolver.class)
    public NameResolver nameResolver(ClientProperties clientProperties) {
        return new FixedAddressNameResolver(clientProperties.getResolver().getAddresses());
    }

    @Bean
    @ConditionalOnMissingBean(Registry.class)
    @ConditionalOnProperty(name = "netty.rpc.client.registry.enabled", matchIfMissing = true)
    public Registry registry() {
        return new NoopRegistry();
    }
}
