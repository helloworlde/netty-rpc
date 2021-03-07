package io.github.helloworlde.netty.protobuf.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtobufClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(workerGroup)
                     .channel(NioSocketChannel.class)
                     .handler(new LoggingHandler(LogLevel.TRACE))
                     .handler(new ProtobufClientChannelInitializer());

            Channel channel = bootstrap.connect("127.0.0.1", 8080)
                                       .addListener(f -> log.info("启动完成"))
                                       .channel();

            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
