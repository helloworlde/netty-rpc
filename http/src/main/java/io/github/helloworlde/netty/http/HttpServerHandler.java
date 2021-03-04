package io.github.helloworlde.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage(), cause);
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest httpContent = (HttpRequest) msg;
            QueryStringDecoder decoder = new QueryStringDecoder(httpContent.uri());
            String params = decoder.parameters()
                                   .entrySet()
                                   .stream()
                                   .map(p -> String.format("%s=%s", p.getKey(), String.join(",", p.getValue())))
                                   .collect(Collectors.joining("\r\n"));

            log.info("读取到新的消息: {}", params);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(params.getBytes(StandardCharsets.UTF_8)));

            response.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .set(HttpHeaderNames.CONTENT_LENGTH, params.length())
                    .set("ServerName", "NETTY_HTTP");


            ChannelFuture future = ctx.write(response);
            future.addListener(ChannelFutureListener.CLOSE);
        } else {
            log.info("msg class is : {}", msg.getClass().getName());
        }
    }
}
