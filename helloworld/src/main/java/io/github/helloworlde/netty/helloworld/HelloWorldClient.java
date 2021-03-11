package io.github.helloworlde.netty.helloworld;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(workerGroup)
                     .channel(NioSocketChannel.class)
                     // 连接超时时间
                     .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                     // 开启 TCP 心跳
                     .option(ChannelOption.SO_KEEPALIVE, true)
                     // 开启 Nagle 算法，true 表示关闭，会立即发送数据
                     .option(ChannelOption.TCP_NODELAY, true)
                     .handler(new ChannelInitializer<NioSocketChannel>() {
                         @Override
                         protected void initChannel(NioSocketChannel ch) throws Exception {
                             ChannelPipeline pipeline = ch.pipeline();
                             pipeline.addLast(new HelloWorldClientHandler());
                         }
                     });

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            log.info("客户端启动完成");
            channelFuture.channel()
                         .closeFuture()
                         .sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
