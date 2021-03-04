package io.github.helloworlde.netty.pojo;

import io.github.helloworlde.netty.pojo.model.CustomMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PojoServerHandler extends SimpleChannelInboundHandler<CustomMessage> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("Read Complete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, CustomMessage msg) throws Exception {
        log.info("收到新的请求: {}", msg.toString());

        ctx.write(CustomMessage.builder()
                               .id(msg.getId())
                               .message("Response: Hello " + msg.getMessage())
                               .timestamp(System.currentTimeMillis())
                               .build())
           .addListener(f -> log.info("发送响应完成"));
    }
}
