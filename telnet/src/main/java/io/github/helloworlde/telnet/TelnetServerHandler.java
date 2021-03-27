package io.github.helloworlde.telnet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String message = String.format("Hello, The address is %s\r\n", ctx.channel().remoteAddress().toString());
        log.info("Channel Active, message is: {}", message);
        ctx.writeAndFlush(message);
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        String pongContent = String.format("PONG for '%s'\r\n", msg);
        log.info("发送响应: {}", pongContent);

        ctx.writeAndFlush(pongContent);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("错误: {}", cause.getMessage(), cause);
    }
}
