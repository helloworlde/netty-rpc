package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.FixedAddressNameResolver;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.registry.NoopRegistry;
import io.github.helloworlde.netty.rpc.registry.Registry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Slf4j
public class ClientBuilder {

    private String authority;

    private SocketAddress serverAddress;

    private NameResolver nameResolver;

    private LoadBalancer loadBalancer;

    private Registry registry;

    private List<ClientInterceptor> interceptors = new ArrayList<>();

    public static ClientBuilder builder() {
        return new ClientBuilder();
    }

    public ClientBuilder forAddress(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
        return this;
    }

    public ClientBuilder forTarget(String authority) {
        this.authority = authority;
        return this;
    }

    public ClientBuilder loadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    public ClientBuilder nameResolver(NameResolver nameResolver) {
        this.nameResolver = nameResolver;
        return this;
    }

    public ClientBuilder registry(Registry registry) {
        this.registry = registry;
        return this;
    }

    public ClientBuilder addInterceptor(ClientInterceptor interceptor) {
        this.interceptors.add(interceptor);
        return this;
    }

    public Client build() {
        if (Objects.isNull(this.loadBalancer)) {
            this.loadBalancer = new RoundRobinLoadBalancer();
        }

        if (Objects.isNull(this.registry)) {
            this.registry = new NoopRegistry();
        }

        if (Objects.nonNull(this.serverAddress)) {
            this.nameResolver = new FixedAddressNameResolver(serverAddress);
        }

        return new Client(authority, nameResolver, loadBalancer, interceptors);
    }
}
