package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
public class RequestProcessor extends SimpleChannelInboundHandler<Request> {

    private final Map<String, ServiceDetail<?>> serviceDetailMap;

    private final Executor executor;

    public RequestProcessor(Map<String, ServiceDetail<?>> serviceDetailMap, Executor executor) {
        this.serviceDetailMap = serviceDetailMap;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        executor.execute(() -> {
            Long requestId = request.getRequestId();
            log.trace("接收到新的请求: {}", requestId);

            Response response = Response.builder()
                                        .requestId(requestId)
                                        .build();
            try {
                Object responseBody = handlerRequest(request);
                log.trace("方法返回结果: {}", responseBody);

                response.setBody(responseBody);
            } catch (RpcException e) {
                log.error("Handler request failed: {}", e.getMessage(), e);
                response.setError(e.getMessage());
            } catch (Exception e) {
                log.error("Handler request error: {}", e.getCause().getMessage(), e);
                response.setError("INTERNAL ERROR");
            } finally {
                ctx.writeAndFlush(response)
                   .addListener(f -> log.trace("发送响应完成"));
            }
        });
    }

    private Object handlerRequest(Request request) throws RpcException, IllegalAccessException, InvocationTargetException {
        String serviceName = Optional.ofNullable(request.getServiceName())
                                     .orElseThrow(() -> new RpcException("ServiceName not present"));
        String methodName = Optional.ofNullable(request.getMethodName())
                                    .orElseThrow(() -> new RpcException("MethodName not present"));

        // Service
        ServiceDetail<?> serviceDetail = serviceDetailMap.get(serviceName);
        if (Objects.isNull(serviceDetail)) {
            throw new RpcException("Service Not Found");
        }
        Method method = serviceDetail.getMethods().get(methodName);
        if (Objects.isNull(method)) {
            throw new RpcException("Method Not Found");
        }

        Object[] params = request.getParams();

        return method.invoke(serviceDetail.getInstance(), params);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.trace("Read Complete");
        // ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage());
    }
}
