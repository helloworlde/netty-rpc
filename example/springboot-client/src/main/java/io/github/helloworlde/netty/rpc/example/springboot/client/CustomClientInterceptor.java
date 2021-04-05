package io.github.helloworlde.netty.rpc.example.springboot.client;

import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomClientInterceptor implements ClientInterceptor {
    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        log.info("执行 CustomClientInterceptor");
        return next.call(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return 1;
    }
}
