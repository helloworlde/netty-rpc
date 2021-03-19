package io.github.helloworlde.netty.rpc.client.handler;

import io.github.helloworlde.netty.rpc.client.ResponseFuture;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestInvoker {

    private final AtomicLong requestSeq = new AtomicLong();
    private Transport transport;

    public RequestInvoker(Transport transport) {
        this.transport = transport;
    }

    private Request createRequest(Class<?> proxyClass, String methodName, Object[] params) throws Exception {
        return Request.builder()
                      .requestId(requestSeq.getAndIncrement())
                      .serviceName(proxyClass.getName())
                      .methodName(methodName)
                      .params(params)
                      .build();
    }

    public Object sendRequest(Class<?> serviceClass, String methodName, Object[] args) throws Exception {
        while (!this.transport.isActive()) {
            log.debug("Channel is not active, waiting...");
        }

        Request request = createRequest(serviceClass, methodName, args);
        ResponseFuture<Object> responseFuture = new ResponseFuture<>();
        transport.write(request, responseFuture);
        return waitResponse(responseFuture);
    }

    private Object waitResponse(ResponseFuture<Object> future) throws Exception {
        return future.get();
    }

}
