package io.github.helloworlde.netty.pojo;

import io.github.helloworlde.netty.pojo.codec.PojoDecoder;
import io.github.helloworlde.netty.pojo.codec.PojoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PojoClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(workerGroup)
                     .channel(NioSocketChannel.class)
                     .handler(new ChannelInitializer<NioSocketChannel>() {
                         @Override
                         protected void initChannel(NioSocketChannel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                             pipeline.addLast(new PojoEncoder());
                             pipeline.addLast(new PojoDecoder());
                             pipeline.addLast(new PojoClientHandler());
                         }
                     });

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
