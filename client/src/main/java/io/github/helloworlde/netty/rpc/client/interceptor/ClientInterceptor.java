package io.github.helloworlde.netty.rpc.client.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;

public interface ClientInterceptor {

    Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception;

    Integer getOrder();
}
