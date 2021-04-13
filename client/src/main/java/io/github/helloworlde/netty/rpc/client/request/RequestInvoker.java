package io.github.helloworlde.netty.rpc.client.request;

import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestInvoker {

    private static final AtomicLong requestSeq = new AtomicLong();

    private Transport transport;

    public RequestInvoker(Transport transport) {
        this.transport = transport;
    }

    public static Request createRequest(Class<?> proxyClass, String methodName, Object... params) {
        return Request.builder()
                      .requestId(getNextSeq())
                      .serviceName(proxyClass.getName())
                      .methodName(methodName)
                      .params(params)
                      .build();
    }

    private synchronized static Long getNextSeq() {
        return requestSeq.getAndIncrement();
    }

    public void sendRequest(Request request, ResponseFuture<Object> responseFuture) throws Exception {
        if (!transport.isActive()) {
            log.warn("{} is not active, try to reconnect", transport);
            transport.doConnect();
        }
        transport.write(request, responseFuture);
    }

    public Object waitResponse(ResponseFuture<Object> future) throws Exception {
        return future.get();
    }

}
