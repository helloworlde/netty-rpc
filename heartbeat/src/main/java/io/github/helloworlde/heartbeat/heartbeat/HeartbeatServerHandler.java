package io.github.helloworlde.heartbeat.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, bytes);

        String message = new String(bytes);

        String pongContent = String.format("PONG for '%s'", message);
        log.info("发送响应: {}", pongContent);

        ByteBuf pong = Unpooled.wrappedBuffer(pongContent.getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(pong);
    }
}
