package io.github.helloworlde.heartbeat.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CustomIdleStateHandler extends IdleStateHandler {

    private final AtomicInteger counter = new AtomicInteger();

    public CustomIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Active");
        // 启动后发送一条消息
        ByteBuf byteBuf = Unpooled.wrappedBuffer("Hello World".getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(byteBuf);
        super.channelActive(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        String message = new String(bytes);
        log.info("收到新的消息: {}", message);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        String message = String.format("PING:%d IDLE_TYPE: %s", counter.getAndIncrement(), evt.state().name());
        log.info("发送消息: {}", message);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
    }
}
