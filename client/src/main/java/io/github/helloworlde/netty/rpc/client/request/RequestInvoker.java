package io.github.helloworlde.netty.rpc.client.request;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RequestInvoker {

    private static final AtomicLong requestSeq = new AtomicLong();

    private final LoadBalancer loadBalancer;

    private final NameResolver nameResolver;

    private final ExecutorService executorService = new ThreadPoolExecutor(10,
            100,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new DefaultThreadFactory("request-group"));

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
        executorService.submit(() -> executeRequest(request, callOptions, responseFuture));
    }

    private void executeRequest(Request request, CallOptions callOptions, ResponseFuture<Object> responseFuture) {
        try {
            if (loadBalancer.getTransports().isEmpty()) {
                log.info("没有可用的 Transport，更新 Transport");
                this.nameResolver.refresh();
            }
            callOptions.checkDeadlineExceeded();

            Transport transport = loadBalancer.chooseTransport();
            callOptions.checkDeadlineExceeded();

            transport.write(request, responseFuture);
        } catch (Exception e) {
            responseFuture.setFailure(e);
        }
    }

    public Object waitResponse(ResponseFuture<Object> future, Long timeout) throws Exception {
        if (timeout <= 0) {
            return future.get();
        } else {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

}
