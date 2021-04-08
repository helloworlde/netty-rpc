package io.github.helloworlde.netty.rpc.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientCall {

    private final ClientInterceptor interceptor;

    private final ClientCall next;

    public ClientCall(ClientInterceptor interceptor) {
        this.next = null;
        this.interceptor = interceptor;
    }

    public ClientCall(ClientCall next, ClientInterceptor interceptor) {
        this.next = next;
        this.interceptor = interceptor;
    }

    public Object call(Request request, CallOptions callOptions) throws Exception {
        return interceptor.interceptorCall(request, callOptions, next);
    }

}
