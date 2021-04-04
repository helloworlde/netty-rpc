package io.github.helloworlde.netty.helloworld.interceptor;

import io.github.helloworlde.netty.rpc.client.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientInterceptorOne implements ClientInterceptor {

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        log.info("ClientInterceptorOne Executed");
        return next.call(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return 0;
    }
}
