package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.client.transport.ClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
public class Client {

    private String authority;

    private LoadBalancer loadBalancer;

    private NameResolver nameResolver;

    private EventLoopGroup workerGroup;

    public Client() {
    }

    public Client(String authority,
                  NameResolver nameResolver,
                  LoadBalancer loadBalancer) {
        this.authority = authority;
        this.nameResolver = nameResolver;
        this.loadBalancer = loadBalancer;
    }

    public Client init() {
        Bootstrap bootstrap = new Bootstrap();
        ClientHandler handler = new ClientHandler();
        workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));

        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new LoggingHandler(LogLevel.TRACE))
                 .handler(new ClientChannelInitializer(handler));

        if (Objects.nonNull(this.loadBalancer)) {
            this.loadBalancer.setBootstrap(bootstrap);
        }

        if (Objects.nonNull(this.nameResolver)) {
            this.nameResolver.setLoadBalancer(loadBalancer);
        }

        return this;
    }

    public Client start() throws Exception {
        if (Objects.isNull(workerGroup)) {
            this.init();
        }

        log.info("Client starting...");
        this.nameResolver.setAuthority(this.authority);
        this.nameResolver.start();
        return this;
    }

    public void shutdown() {
        try {
            log.info("Start shutting down...");
            this.loadBalancer.shutdown();
            this.nameResolver.shutdown();
            this.workerGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        } finally {
            log.info("Shutting down completed.");
        }
    }

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }

}
