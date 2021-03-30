package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.lb.RandomLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.FixedAddressNameResolver;
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
public class Client {

    private SocketAddress address;

    private LoadBalancer loadBalancer;

    private NameResolver nameResolver;

    private String authority;

    private ScheduledExecutorService executor;

    private EventLoopGroup workerGroup;

    private Client() {
    }

    public static Client client() {
        return new Client();
    }

    public Client forAddress(String host, int port) {
        this.address = new InetSocketAddress(host, port);
        return this;
    }

    public Client forTarget(String authority) {
        this.authority = authority;
        return this;
    }

    public Client nameResolver(NameResolver nameResolver) {
        this.nameResolver = nameResolver;
        return this;
    }

    public Client loadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        return this;
    }

    public Client start() throws Exception {
        log.info("Client starting...");
        this.executor = new ScheduledThreadPoolExecutor(5, new DefaultThreadFactory("name-resolver"));

        Bootstrap bootstrap = new Bootstrap();
        ClientHandler handler = new ClientHandler();
        workerGroup = new NioEventLoopGroup(100, new DefaultThreadFactory("transport-io"));

        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new LoggingHandler(LogLevel.TRACE))
                 .handler(new ClientChannelInitializer(handler));

        if (Objects.isNull(this.loadBalancer)) {
            this.loadBalancer = new RandomLoadBalancer();
        }
        this.loadBalancer.setBootstrap(bootstrap);
        if (Objects.nonNull(this.nameResolver)) {
            this.nameResolver.setAuthority(this.authority);
            this.executor.scheduleAtFixedRate(() -> nameResolver.resolve(), 5, 20, TimeUnit.SECONDS);
        } else {
            this.nameResolver = new FixedAddressNameResolver(address);
        }
        this.nameResolver.setLoadBalancer(loadBalancer);
        this.nameResolver.resolve();
        return this;
    }

    public void shutdown() {
        try {
            log.info("Start shutting down...");
            this.loadBalancer.shutdown();
            this.executor.shutdown();
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

    public NameResolver getNameResolver() {
        return nameResolver;
    }
}
