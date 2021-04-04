package io.github.helloworlde.netty.helloworld.interceptor;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInterceptorTwo implements ClientInterceptor {
    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        log.info("执行客户端拦截器 Two");
        callOptions.withTimeout(1000L);
        return next.call(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return 2;
    }
}
