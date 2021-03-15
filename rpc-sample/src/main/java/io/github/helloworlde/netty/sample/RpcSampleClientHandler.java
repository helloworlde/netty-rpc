package io.github.helloworlde.netty.sample;

import io.github.helloworlde.netty.rpc.model.Header;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.sample.service.HelloService;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RpcSampleClientHandler extends SimpleChannelInboundHandler<Response> {

    private final AtomicLong counter = new AtomicLong();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Active, 发送消息");
        ctx.executor()
           .scheduleAtFixedRate(() -> {
               ctx.writeAndFlush(Request.builder()
                                        .requestId(counter.getAndIncrement())
                                        .header(Header.builder()
                                                      .serviceName(HelloService.class.getName())
                                                      .methodName("sayHello")
                                                      .build())
                                        .body("RPC")
                                        .build())
                  .addListener((ChannelFutureListener) future -> log.info("发送请求完成"));

           }, 2, 2, TimeUnit.SECONDS);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Read Complete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常: {}", cause.getMessage(), cause);
        cause.printStackTrace();
        // ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.info("收到新的响应: {}", msg.toString());
    }
}
