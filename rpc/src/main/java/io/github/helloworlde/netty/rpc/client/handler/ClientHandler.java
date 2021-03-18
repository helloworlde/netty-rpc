package io.github.helloworlde.netty.rpc.client.handler;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.model.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Response> {

    private Channel channel;

    private Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.debug("接收到响应: {}", msg.getRequestId());
        Long requestId = msg.getRequestId();
        try {
            client.receiveResponse(msg);
        } catch (Exception e) {
            client.receiveError(requestId, e);
        } finally {
            client.completeRequest(requestId);
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
}
