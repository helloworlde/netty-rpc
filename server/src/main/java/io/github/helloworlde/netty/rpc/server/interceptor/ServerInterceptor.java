package io.github.helloworlde.netty.rpc.server.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;

public interface ServerInterceptor {

    Object interceptorCall(Request request, Metadata metadata, ServerCall next) throws Exception;

    Integer getOrder();
}
