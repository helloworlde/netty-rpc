package io.github.helloworlde.heartbeat.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HeartbeatClientHandler extends ChannelInboundHandlerAdapter {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Active");
        // 启动后发送一条消息
        ByteBuf byteBuf = Unpooled.wrappedBuffer("Hello World".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage(), cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, bytes);

        String message = new String(bytes);
        log.info("收到新的消息: {}", message);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            String message = String.format("PING:%d IDLE_TYPE: %s", counter.getAndIncrement(), idleStateEvent.state().name());

            log.info("发送消息: {}", message);

            ctx.writeAndFlush(Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
        }
    }

}
