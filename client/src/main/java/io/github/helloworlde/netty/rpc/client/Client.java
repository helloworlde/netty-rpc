package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.client.transport.ClientChannelInitializer;
import io.github.helloworlde.netty.rpc.client.transport.TransportFactory;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.registry.Registry;
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

import java.util.List;
import java.util.Objects;

@Data
@Slf4j
public class Client {

    private String authority;

    private LoadBalancer loadBalancer;

    private NameResolver nameResolver;

    private Registry registry;

    private EventLoopGroup workerGroup;

    private List<ClientInterceptor> interceptors;

    private boolean enableHeartbeat = true;

    private Long timeout = 10_000L;

    public Client() {
    }

    public Client(String authority,
                  NameResolver nameResolver,
                  LoadBalancer loadBalancer,
                  List<ClientInterceptor> interceptors,
                  Long timeout) {
        this.authority = authority;
        this.nameResolver = nameResolver;
        this.loadBalancer = loadBalancer;
        this.interceptors = interceptors;
        this.timeout = timeout;
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

        TransportFactory transportFactory = new TransportFactory(bootstrap, enableHeartbeat);

        if (Objects.nonNull(this.loadBalancer)) {
            this.loadBalancer.setTransportFactory(transportFactory);
        }

        if (Objects.nonNull(this.nameResolver)) {
            this.nameResolver.setLoadBalancer(loadBalancer);
        }

        return this;
    }

    public void start() throws Exception {
        if (Objects.isNull(workerGroup)) {
            this.init();
        }

        log.debug("Client starting...");
        this.nameResolver.setAuthority(this.authority);
        this.nameResolver.start();
    }

    public void shutdown() {
        try {
            log.debug("Start shutting down...");
            this.loadBalancer.shutdown();
            this.nameResolver.shutdown();
            this.workerGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        } finally {
            log.debug("Shutting down completed.");
        }
    }

    public LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }

}
