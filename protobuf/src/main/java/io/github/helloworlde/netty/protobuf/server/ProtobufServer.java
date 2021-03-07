package io.github.helloworlde.netty.protobuf.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtobufServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                           .channel(NioServerSocketChannel.class)
                           .handler(new LoggingHandler(LogLevel.TRACE))
                           .childHandler(new ProtobufServerChannelInitializer());

            ChannelFuture future = serverBootstrap.bind(8080)
                                                  .sync()
                                                  .addListener(f -> log.info("Server 启动完成"));
            future.channel()
                  .closeFuture()
                  .sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
