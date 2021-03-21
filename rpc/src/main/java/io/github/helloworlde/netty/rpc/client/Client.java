package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.lb.RandomLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Client {

    private Transport transport;

    private SocketAddress address;

    private LoadBalancer loadBalancer = new RandomLoadBalancer();

    private NameResolver nameResolver;

    private String authority;

    private ScheduledExecutorService executor;

    public Client forAddress(String host, int port) throws Exception {
        this.address = new InetSocketAddress(host, port);
        loadBalancer.updateAddress(Collections.singletonList(address));
        return this;
    }

    public Client forTarget(String authority) {
        this.authority = authority;
        return this;
    }

    public Client loadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    public Client start() throws Exception {
        log.info("Client starting...");
        this.executor = new ScheduledThreadPoolExecutor(5, new DefaultThreadFactory("name-resolver"));
        if (Objects.nonNull(this.authority)) {
            this.nameResolver = new ConsulNameResolver(this.authority, this.loadBalancer);
            this.nameResolver.resolve();
            this.executor.scheduleAtFixedRate(() -> nameResolver.resolve(), 5, 20, TimeUnit.SECONDS);
        }
        return this;
    }

    public void shutdown() {
        try {
            log.info("Shutting down...");
            this.executor.shutdown();
            this.transport.shutdown();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        }
    }

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }

    public NameResolver getNameResolver() {
        return nameResolver;
    }
}
