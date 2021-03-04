package io.github.helloworlde.netty.pojo;

import io.github.helloworlde.netty.pojo.codec.PojoDecoder;
import io.github.helloworlde.netty.pojo.codec.PojoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PojoServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                           .channel(NioServerSocketChannel.class)
                           .handler(new LoggingHandler(LogLevel.DEBUG))
                           .childHandler(new ChannelInitializer<SocketChannel>() {
                               @Override
                               protected void initChannel(SocketChannel ch) throws Exception {
                                   ChannelPipeline pipeline = ch.pipeline();
                                   pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                                   pipeline.addLast(new PojoDecoder());
                                   pipeline.addLast(new PojoEncoder());
                                   pipeline.addLast(new PojoServerHandler());
                               }
                           });

            ChannelFuture future = serverBootstrap.bind(8080).sync();
            log.info("Server 启动完成");
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
