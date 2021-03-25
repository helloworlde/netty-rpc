package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.ServiceDefinition;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class RequestProcessor {

    private final ServiceRegistry serviceRegistry;

    public RequestProcessor(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void process(Channel channel, Request request) {
        log.info("开始处理请求: {}", request);
        Response response = Response.builder()
                                    .requestId(request.getRequestId())
                                    .build();
        try {
            String serviceName = Optional.ofNullable(request.getServiceName())
                                         .orElseThrow(() -> new RpcException("ServiceName not present"));
            String methodName = Optional.ofNullable(request.getMethodName())
                                        .orElseThrow(() -> new RpcException("MethodName not present"));

            // Service
            ServiceDefinition<?> serviceDefinition = serviceRegistry.getService(serviceName);

            Method method = Optional.ofNullable(serviceDefinition.getMethods().get(methodName))
                                    .orElseThrow(() -> new RpcException("Method Not Found"));

            Object[] params = request.getParams();
            Object responseBody = doInvoke(method, serviceDefinition.getInstance(), params);
            log.info("方法返回结果: {}", responseBody);

            response.setBody(responseBody);
        } catch (RpcException e) {
            log.error("Handler request failed: {}", e.getMessage(), e);
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("Handler request error: {}", e.getCause().getMessage(), e);
            response.setError("INTERNAL ERROR");
        } finally {
            channel.writeAndFlush(response)
                   .addListener(f -> log.info("发送响应完成"));
        }
    }

    private Object doInvoke(Method method, Object instance, Object[] params) throws Exception {
        log.info("开始调用方法: {}#{}", instance.getClass().getName(), method.getName());
        return method.invoke(instance, params);
    }

}
