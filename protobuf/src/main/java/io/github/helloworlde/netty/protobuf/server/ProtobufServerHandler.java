package io.github.helloworlde.netty.protobuf.server;

import io.github.helloworlde.netty.protobuf.proto.HelloRequest;
import io.github.helloworlde.netty.protobuf.proto.HelloResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtobufServerHandler extends SimpleChannelInboundHandler<HelloRequest> {

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
    public void channelRead0(ChannelHandlerContext ctx, HelloRequest request) throws Exception {
        log.info("收到新的请求: {}", request.toString());

        ctx.write(HelloResponse.newBuilder()
                               .setId(request.getId())
                               .setMessage("Response: Hello " + request.getMessage())
                               .setTimestamp(System.currentTimeMillis())
                               .build())
           .addListener(f -> log.info("发送响应完成"));

    }
}
