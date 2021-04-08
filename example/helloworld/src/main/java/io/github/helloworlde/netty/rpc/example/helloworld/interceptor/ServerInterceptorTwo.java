package io.github.helloworlde.netty.rpc.example.helloworld.interceptor;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.interceptor.ServerInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerInterceptorTwo implements ServerInterceptor {

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ServerCall next) throws Exception {
        log.info("执行服务端拦截器 Two");
        return next.call(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return 2;
    }
}
