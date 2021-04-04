package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestInterceptor implements ClientInterceptor {

    private final RequestInvoker invoker;

    public RequestInterceptor(RequestInvoker invoker) {
        this.invoker = invoker;
    }

    public Object sendRequest(Request request, CallOptions callOptions) throws Exception {
        request.setExtra(callOptions.getAttributes());

        ResponseFuture<Object> responseFuture = new ResponseFuture<>();

        invoker.sendRequest(request, callOptions, responseFuture);

        return invoker.waitResponse(responseFuture, callOptions.getTimeout());
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        return sendRequest(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }

}
