package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.lb.RandomLoadBalancer;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


@Slf4j
public class Client {

    private Transport transport;

    private SocketAddress address;

    private LoadBalancer loadBalancer = new RandomLoadBalancer();

    public Client forAddress(String host, int port) throws Exception {
        this.address = new InetSocketAddress(host, port);
        loadBalancer.updateAddress(address);
        return this;
    }

    public Client loadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    public Client start() throws Exception {
        log.info("Client starting...");
        transport = new Transport(this.address);
        transport.init();
        transport.doConnect();
        return this;
    }

    public void shutdown() {
        try {
            log.info("Shutting down...");
            transport.shutdown();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        }
    }

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }
}
