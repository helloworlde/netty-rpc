package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceProxyClientCall extends ClientCall {

    private final RequestInvoker invoker;

    public ServiceProxyClientCall(RequestInvoker invoker) {
        super(null, null);
        this.invoker = invoker;
    }

    @Override
    public ClientCall newCall(Request request, CallOptions callOptions) throws Exception {
        return this;
    }

    public Object sendRequest(Request request, CallOptions callOptions) throws Exception {
        request.setExtra(callOptions.getAttributes());

        ResponseFuture<Object> responseFuture = new ResponseFuture<>();

        invoker.sendRequest(request, callOptions, responseFuture);

        return invoker.waitResponse(responseFuture, callOptions.getTimeout());
    }
}
