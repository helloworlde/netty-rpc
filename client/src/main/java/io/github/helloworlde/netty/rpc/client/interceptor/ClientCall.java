package io.github.helloworlde.netty.rpc.client.interceptor;

import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientCall {

    private final ClientInterceptor interceptor;

    private final ClientCall clientCall;

    public ClientCall(ClientCall clientCall, ClientInterceptor interceptor) {
        this.clientCall = clientCall;
        this.interceptor = interceptor;
    }

    public ClientCall newCall(Request request, CallOptions callOptions) throws Exception {
        return interceptor.interceptorCall(request, callOptions, clientCall);
    }

}
