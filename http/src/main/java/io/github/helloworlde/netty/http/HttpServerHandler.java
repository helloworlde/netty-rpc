package io.github.helloworlde.netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

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
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        log.info("请求方法: {}, URI:{} ", msg.method().name(), msg.uri());

        StringBuilder content = new StringBuilder();
        appendContent(content, msg.method().name(), "Method");
        appendContent(content, msg.uri(), "URI");

        String headers = msg.headers()
                            .entries()
                            .stream()
                            .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                            .collect(Collectors.joining("\r\n"));

        appendContent(content, headers, "Headers");

        if (HttpMethod.GET.equals(msg.method())) {
            // 解析路径参数
            String param = parseQueryParams(msg.uri(), true);
            appendContent(content, param, "Param");
        } else if (HttpMethod.POST.equals(msg.method())) {
            // 解析请求类型
            String contentType = msg.headers().get(HttpHeaderNames.CONTENT_TYPE);
            appendContent(content, contentType, "ContentType");

            // 解析路径参数
            String param = parseQueryParams(msg.uri(), true);
            appendContent(content, param, "Param");

            if (HttpHeaderValues.APPLICATION_JSON.toString().equals(contentType)) {
                // 解析 JSON Body
                String body = msg.content().toString(StandardCharsets.UTF_8);
                appendContent(content, body, "Body");
            } else if (HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString().equals(contentType)) {
                // 解析 Form Body
                String form = parseQueryParams(msg.content().toString(StandardCharsets.UTF_8), false);
                appendContent(content, form, "Form");
            } else {
                log.info("Others ContentType: {}", contentType);
            }
        }


        String responseContent = content.toString();
        log.info("响应内容:\n{}", responseContent);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseContent.getBytes(StandardCharsets.UTF_8)));

        // 添加响应 Header
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, responseContent.length())
                .set("ServerName", "NETTY_HTTP");

        ChannelFuture future = ctx.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    private void appendContent(StringBuilder content, String param, String type) {
        content.append(String.format("-------------%s-------------", type))
               .append("\r\n")
               .append(param)
               .append("\r\n");
    }

    /**
     * 解析路径参数
     */
    private String parseQueryParams(String params, boolean hasPath) {
        QueryStringDecoder decoder = new QueryStringDecoder(params, hasPath);
        return decoder.parameters()
                      .entrySet()
                      .stream()
                      .map(e -> String.format("%s=%s", e.getKey(), String.join(",", e.getValue())))
                      .collect(Collectors.joining("\r\n"));
    }
}
