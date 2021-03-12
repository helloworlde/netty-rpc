package io.github.helloworlde.netty.http2.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class Http2ClientHandler extends SimpleChannelInboundHandler<Http2StreamFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2StreamFrame msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headers = (Http2HeadersFrame) msg;
            log.info("接收到 Header: {}", headers);
        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame data = (Http2DataFrame) msg;
            String content = data.content().toString(StandardCharsets.UTF_8);
            log.info("接收到 Data: {}", content);
        } else {
            log.info("接收到响应: {}", msg.getClass());
        }
    }

}
