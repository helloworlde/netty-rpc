package io.github.helloworlde.netty.rpc.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;

public interface ServerInterceptor {

    Object interceptorCall(Request request, CallOptions callOptions, ServerCall next) throws Exception;

    Integer getOrder();
}
