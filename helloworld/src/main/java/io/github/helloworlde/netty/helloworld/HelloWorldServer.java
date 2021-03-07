package io.github.helloworlde.netty.helloworld;

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
public class HelloWorldServer {

    public static void main(String[] args) throws InterruptedException {
        // bossGroup 处理连接和 IO 事件
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // workerGroup 执行逻辑处理
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                           // 用于接收客户端的连接
                           .channel(NioServerSocketChannel.class)
                           // 打印日志
                           .handler(new LoggingHandler(LogLevel.DEBUG))
                           .childHandler(
                                   // 在接收到客户端连接后会回调 initChannel 进行初始化
                                   new ChannelInitializer<SocketChannel>() {
                                       @Override
                                       protected void initChannel(SocketChannel ch) throws Exception {
                                           ChannelPipeline pipeline = ch.pipeline();
                                           pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                                           pipeline.addLast(new HelloWorldServerHandler());
                                       }
                                   });

            // 监听端口
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
