package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.server.interceptor.Metadata;
import io.github.helloworlde.netty.rpc.server.interceptor.ServerCall;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;

@Slf4j
public class RequestProcessor {

    private final ServerCall serverCall;

    public RequestProcessor(ServerCall serverCall) {
        this.serverCall = serverCall;
    }

    public void process(Channel channel, Request request) {
        log.info("开始处理请求: {}", request);

        Response response = Response.builder()
                                    .requestId(request.getRequestId())
                                    .build();

        try {
            // Metadata
            SocketAddress remoteAddress = channel.remoteAddress();
            Metadata metadata = new Metadata();

            Map<String, Object> extra = request.getExtra();
            extra.forEach(metadata::withAttribute);
            metadata.withAttribute("remoteAddress", remoteAddress);
            metadata.withAttribute("requestId", request.getRequestId());

            // 调用
            Object responseBody = serverCall.call(request, metadata);
            response.setBody(responseBody);
        } catch (RpcException e) {
            log.error("Handler request failed: {}", e.getMessage(), e);
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("Handler request error: {}", e.getMessage(), e);
            response.setError("INTERNAL ERROR");
        } finally {
            channel.writeAndFlush(response)
                   .addListener(f -> log.info("发送响应完成"));
        }
    }
}
