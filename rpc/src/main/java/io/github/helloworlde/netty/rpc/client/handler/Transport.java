package io.github.helloworlde.netty.rpc.client.handler;

import io.github.helloworlde.netty.rpc.client.ResponseFuture;
import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.Status;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class Transport extends SimpleChannelInboundHandler<Response> {

    private ConcurrentHashMap<Long, ResponseFuture<Object>> paddingRequests = new ConcurrentHashMap<>();

    private Channel channel;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.info("接收到响应: {}", msg.getRequestId());
        Long requestId = msg.getRequestId();
        try {
            if (Status.SUCCESS.equals(msg.getStatus())) {
                paddingRequests.get(msg.getRequestId())
                               .setSuccess(msg.getBody());
            } else {
                paddingRequests.get(msg.getRequestId())
                               .setFailure(new RpcException((String) msg.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            paddingRequests.remove(requestId);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel Active");
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

    public ResponseFuture<Object> sendRequest(Request request) {
        while (!this.channel.isActive()) {
            log.info("Channel is not active, waiting...");
        }

        ResponseFuture<Object> responseFuture = new ResponseFuture<>();
        this.paddingRequests.putIfAbsent(request.getRequestId(), responseFuture);
        channel.writeAndFlush(request)
               .addListener(f -> {
                   if (f.isSuccess()) {
                       log.info("请求: {} 发送完成", request.getRequestId());
                   } else {
                       log.error("请求: {} 发送失败: {} ", request.getRequestId(), f.cause().getMessage());
                       this.paddingRequests.get(request.getRequestId())
                                           .setFailure(f.cause());
                   }
               });
        return responseFuture;
    }
}
