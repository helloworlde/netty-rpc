package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class RequestProcessor {

    private final Map<String, ServiceDetail<?>> serviceDetailMap;

    public RequestProcessor(Map<String, ServiceDetail<?>> serviceDetailMap) {
        this.serviceDetailMap = serviceDetailMap;
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
            ServiceDetail<?> serviceDetail = serviceDetailMap.get(serviceName);
            if (Objects.isNull(serviceDetail)) {
                throw new RpcException("Service Not Found");
            }
            Method method = serviceDetail.getMethods().get(methodName);
            if (Objects.isNull(method)) {
                throw new RpcException("Method Not Found");
            }

            Object[] params = request.getParams();
            Object responseBody = doInvoke(method, serviceDetail.getInstance(), params);
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
