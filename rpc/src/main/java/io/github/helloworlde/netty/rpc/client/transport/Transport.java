package io.github.helloworlde.netty.rpc.client.transport;

import io.github.helloworlde.netty.rpc.client.ResponseFuture;
import io.github.helloworlde.netty.rpc.client.handler.ClientHandler;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Transport {

    private static EventLoopGroup workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));

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
        handler = new ClientHandler();
        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                 .channel(NioSocketChannel.class)
                 .handler(new LoggingHandler(LogLevel.DEBUG))
                 .handler(new ClientChannelInitializer(handler));
        init.compareAndSet(false, true);
    }

    public void doConnect() throws Exception {
        if (!isInit()) {
            init();
        }

        this.channel = bootstrap.connect(address)
                                .sync()
                                .addListener(f -> log.debug("Client 启动成功"))
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
        this.channel.flush();
        this.channel.disconnect().addListener(f -> log.debug("Disconnect completed"));
        workerGroup.shutdownGracefully().addListener(f -> log.debug("WorkerGroup shutdown complete"));
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {
        handler.write(request, responseFuture);
    }
}
