package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.codec.MessageDecoder;
import io.github.helloworlde.netty.rpc.codec.MessageEncoder;
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
public class RpcSampleClient {

    public static void main(String[] args) {
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
                             pipeline.addLast(new MessageEncoder());
                             pipeline.addLast(new MessageDecoder());
                             pipeline.addLast(new RpcSampleClientHandler());
                         }
                     });

            Channel channel = bootstrap.connect("127.0.0.1", 9090)
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
