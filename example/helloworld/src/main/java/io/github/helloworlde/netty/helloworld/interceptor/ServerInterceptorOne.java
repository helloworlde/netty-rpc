package io.github.helloworlde.netty.helloworld.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.server.interceptor.Metadata;
import io.github.helloworlde.netty.rpc.server.interceptor.ServerCall;
import io.github.helloworlde.netty.rpc.server.interceptor.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerInterceptorOne implements ServerInterceptor {

    @Override
    public Object interceptorCall(Request request, Metadata metadata, ServerCall next) throws Exception {
        log.info("执行服务端拦截器 One");
        return next.call(request, metadata);
    }

    @Override
    public Integer getOrder() {
        return 1;
    }
}
