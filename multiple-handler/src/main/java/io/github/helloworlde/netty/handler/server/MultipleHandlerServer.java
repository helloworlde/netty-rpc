package io.github.helloworlde.netty.handler.server;

import io.github.helloworlde.netty.handler.codec.PojoDecoder;
import io.github.helloworlde.netty.handler.codec.PojoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultipleHandlerServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

            serverBootstrap.group(bossGroup, workerGroup)
                           .channel(NioServerSocketChannel.class)
                           .handler(new LoggingHandler(LogLevel.DEBUG))
                           .childHandler(new ChannelInitializer<SocketChannel>() {
                               @Override
                               protected void initChannel(SocketChannel ch) throws Exception {
                                   ChannelPipeline pipeline = ch.pipeline();
                                   // 添加多个 handler，支持 HTTP 和自定义的编解码
                                   pipeline.addLast(new PojoDecoder())
                                           .addLast(new PojoEncoder())
                                           .addLast(new PojoServerHandler())
                                           .addLast(new HttpServerCodec())
                                           .addLast(new HttpServerExpectContinueHandler())
                                           .addLast(new HttpServerHandler())
                                   ;
                               }
                           });

            ChannelFuture future = serverBootstrap.bind(8080)
                                                  .addListener(f -> log.info("启动完成"))
                                                  .sync();
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
