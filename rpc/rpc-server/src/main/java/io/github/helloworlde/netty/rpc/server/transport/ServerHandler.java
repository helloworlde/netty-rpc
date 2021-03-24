package io.github.helloworlde.netty.rpc.server.transport;

import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.server.handler.RequestProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    private final RequestProcessor processor;

    private final Executor executor;

    private Channel channel;

    public ServerHandler(RequestProcessor processor, Executor executor) {
        this.processor = processor;
        this.executor = executor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Active: {}", ctx.channel());
        this.channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Inactive: {}", ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        log.info("接收到新的请求: {}", request.getRequestId());

        executor.execute(() -> {
            this.processor.process(channel, request);
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("Read Complete");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage(), cause);
    }
}
