package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Header;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.github.helloworlde.netty.rpc.model.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class RequestProcessor extends SimpleChannelInboundHandler<Request> {

    private final Map<String, ServiceDetail<?>> serviceDetailMap;

    public RequestProcessor(Map<String, ServiceDetail<?>> serviceDetailMap) {
        this.serviceDetailMap = serviceDetailMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        Long requestId = request.getRequestId();
        log.info("接收到新的请求: {}", requestId);

        // header
        Header header = request.getHeader();
        String serviceName = Optional.ofNullable(header.getServiceName())
                                     .orElseThrow(() -> new RpcException("ServiceName not present"));
        String methodName = Optional.ofNullable(header.getMethodName())
                                    .orElseThrow(() -> new RpcException("MethodName not present"));

        // Service
        ServiceDetail<?> serviceDetail = serviceDetailMap.get(serviceName);
        if (Objects.isNull(serviceDetail)) {
            throw new RpcException(requestId, "Service Not Found");
        }
        Method method = serviceDetail.getMethods().get(methodName);
        if (Objects.isNull(method)) {
            throw new RpcException(requestId, "Method Not Found");
        }

        Object body = request.getBody();
        Object responseBody = method.invoke(serviceDetail.getInstance(), body);
        log.info("方法返回结果: {}", responseBody);

        Response response = Response.builder()
                                    .requestId(requestId)
                                    .body(responseBody)
                                    .status(Status.SUCCESS)
                                    .build();
        ctx.write(response)
           .addListener(f -> log.info("发送响应完成"));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("Read Complete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage());

        RpcException rpcException;
        if (cause instanceof RpcException) {
            rpcException = (RpcException) cause;
        } else {
            rpcException = RpcException.builder()
                                       .message(cause.getMessage())
                                       .throwable(cause)
                                       .build();
        }
        Response response = Response.builder()
                                    .requestId(rpcException.getRequestId())
                                    .body(rpcException.getMessage())
                                    .status(Status.EXCEPTION)
                                    .build();

        ctx.writeAndFlush(response);
    }
}
