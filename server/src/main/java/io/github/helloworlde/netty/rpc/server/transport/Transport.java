package io.github.helloworlde.netty.rpc.server.transport;

import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Transport {

    private final AtomicBoolean init = new AtomicBoolean(false);

    ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private ThreadPoolExecutor executor;

    private Channel channel;

    public synchronized void doInit(RequestProcessor requestProcessor) {
        if (init.get()) {
            return;
        }
        log.info("Transport init");

        bossGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("accept-group"));
        workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("io-event-group"));
        executor = new ThreadPoolExecutor(10, 100, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("business-group"));

        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                       .channel(NioServerSocketChannel.class)
                       .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                       .handler(new LoggingHandler(LogLevel.DEBUG))
                       .childHandler(new ServerChannelInitializer(requestProcessor, executor));

        this.init.compareAndSet(false, true);
    }

    public int doBind(int port) throws InterruptedException {
        log.info("Transport bind  port: {} ", port);
        ChannelFuture channelFuture = serverBootstrap.bind(port)
                                                     .addListener(f -> {
                                                         if (f.isSuccess()) {
                                                             log.info("Server 绑定端口: {} 完成", port);
                                                         }
                                                     });

        channelFuture.await();
        if (!channelFuture.isSuccess()) {
            log.info("Server 绑定端口: {} 失败，尝试其他端口", port);
            return doBind(port + 1);
        }
        this.channel = channelFuture.channel();
        return port;
    }


    public void shutdown() {
        log.info("Transport shutting downing...");
        this.bossGroup.shutdownGracefully();
        this.workerGroup.shutdownGracefully();
        this.executor.shutdown();
    }

    public void awaitTermination() throws InterruptedException {
        log.info("Transport running...");
        this.channel.closeFuture().sync();
    }
}
