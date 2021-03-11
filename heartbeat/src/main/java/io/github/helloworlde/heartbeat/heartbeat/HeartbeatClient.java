package io.github.helloworlde.heartbeat.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatClient {

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
                             // 添加空闲处理，空闲后发送心跳
                             pipeline.addLast(new CustomIdleStateHandler(10, 10, 10));
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
