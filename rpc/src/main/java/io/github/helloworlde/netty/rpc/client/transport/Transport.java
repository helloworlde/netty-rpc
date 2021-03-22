package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.ResponseFuture;
import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Transport {

    private EventLoopGroup workerGroup;

    private Channel channel;

    private Bootstrap bootstrap;

    private ClientHandler handler;

    private SocketAddress address;

    private final AtomicBoolean init = new AtomicBoolean(false);

    public Transport(SocketAddress address) {
        this.address = address;
    }

    public void init() throws Exception {
        if (isInit()) {
            return;
        }
        log.info("开始初始化: {}", this.address);

        this.handler = new ClientHandler();
        this.workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));
        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new LoggingHandler(LogLevel.DEBUG))
                 .handler(new ClientChannelInitializer(handler));
        init.compareAndSet(false, true);
    }

    public void doConnect() throws Exception {
        if (!isInit()) {
            init();
        }

        if (Objects.nonNull(channel) && channel.isActive()) {
            return;
        }
        log.info("开始连接: {}", this.address);

        this.channel = bootstrap.connect(address)
                                .sync()
                                .addListener(f -> log.info("连接: {} 成功", this.address))
                                .channel();
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public boolean isInit() {
        return this.init.get();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void shutdown() {
        log.info("开始关闭连接: {}", this.address);
        this.channel.flush();
        this.channel.disconnect().addListener(f -> log.debug("Disconnect completed"));
        workerGroup.shutdownGracefully().addListener(f -> log.debug("WorkerGroup shutdown complete"));
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {
        log.info("请求: {}", this.address);
        handler.write(request, responseFuture);
    }
}
