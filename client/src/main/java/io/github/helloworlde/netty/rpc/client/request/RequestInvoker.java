package io.github.helloworlde.netty.rpc.client.request;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestInvoker {

    private static final AtomicLong requestSeq = new AtomicLong();

    private final LoadBalancer loadBalancer;

    private final NameResolver nameResolver;

    public RequestInvoker(LoadBalancer loadBalancer, NameResolver nameResolver) {
        this.loadBalancer = loadBalancer;
        this.nameResolver = nameResolver;
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

    public void sendRequest(Request request, CallOptions callOptions, ResponseFuture<Object> responseFuture) throws Exception {
        if (loadBalancer.getTransports().isEmpty()) {
            log.info("没有可用的 Transport，更新 Transport");
            this.nameResolver.refresh();
        }

        Transport transport = loadBalancer.chooseTransport();
        transport.write(request, responseFuture);
    }

    public Object waitResponse(ResponseFuture<Object> future, Long timeout) throws Exception {
        if (timeout <= 0) {
            return future.get();
        } else {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

}
