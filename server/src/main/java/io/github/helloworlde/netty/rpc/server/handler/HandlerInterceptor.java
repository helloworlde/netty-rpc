package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.interceptor.ServerInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.ServiceDefinition;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class HandlerInterceptor implements ServerInterceptor {

    private final ServiceRegistry serviceRegistry;

    public HandlerInterceptor(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ServerCall next) throws Exception {

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
        log.debug("方法返回结果: {}", responseBody);
        return responseBody;
    }


    private Object doInvoke(Method method, Object instance, Object[] params) throws Exception {
        log.debug("开始调用方法: {}#{}", instance.getClass().getName(), method.getName());
        return method.invoke(instance, params);
    }


    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }
}
