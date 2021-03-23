package io.github.helloworlde.netty.rpc.client.handler;

import io.github.helloworlde.netty.rpc.client.ResponseFuture;
import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Response> {

    private Channel channel;

    private final ConcurrentHashMap<Long, ResponseFuture<Object>> paddingRequests = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.debug("接收到响应: {}", msg.getRequestId());
        Long requestId = msg.getRequestId();
        try {
            receiveResponse(msg);
        } catch (Exception e) {
            receiveError(requestId, e);
        } finally {
            completeRequest(requestId);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel Active");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel Read Complete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常: {}", cause.getMessage(), cause);
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {
        this.paddingRequests.putIfAbsent(request.getRequestId(), responseFuture);
        channel.writeAndFlush(request)
               .addListener(f -> {
                   if (f.isSuccess()) {
                       log.trace("请求: {} 发送完成", request.getRequestId());
                       sendComplete(request.getRequestId());
                   } else {
                       log.debug("请求: {} 发送失败: {} ", request.getRequestId(), f.cause().getMessage());
                       receiveError(request.getRequestId(), f.cause());
                   }
               });
    }

    public void receiveResponse(Response msg) {
        if (msg.getError() == null) {
            paddingRequests.get(msg.getRequestId())
                           .setSuccess(msg.getBody());
        } else {
            receiveError(msg.getRequestId(), new RpcException("Response failed: " + msg.getError()));
        }
    }

    public void receiveError(Long requestId, Throwable cause) {
        paddingRequests.get(requestId)
                       .setFailure(cause);
    }

    public void sendComplete(Long requestId) {
        log.trace("Send request: {} success", requestId);
    }

    public void completeRequest(Long requestId) {
        log.trace("Request: {} Completed", requestId);
        this.paddingRequests.remove(requestId);
    }
}
