package io.github.helloworlde.netty.rpc.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerCall {

    private final ServerInterceptor interceptor;

    private final ServerCall serverCall;

    public ServerCall(ServerInterceptor interceptor) {
        this.serverCall = null;
        this.interceptor = interceptor;
    }

    public ServerCall(ServerCall serverCall, ServerInterceptor interceptor) {
        this.serverCall = serverCall;
        this.interceptor = interceptor;
    }

    public Object call(Request request, Metadata metadata) throws Exception {
        return interceptor.interceptorCall(request, metadata, serverCall);
    }

}
