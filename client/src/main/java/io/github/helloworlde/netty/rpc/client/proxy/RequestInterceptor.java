package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.client.request.ResponseFuture;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RequestInterceptor implements ClientInterceptor {

    private final RequestInvoker invoker;

    private final ExecutorService executorService;

    private final EventExecutor eventExecutor;

    public RequestInterceptor(RequestInvoker invoker) {
        this.invoker = invoker;
        this.executorService = new ThreadPoolExecutor(10,
                100,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("request-group"));
        this.eventExecutor = new DefaultEventExecutor(executorService);
    }

    public Object sendRequest(Request request, CallOptions callOptions) throws Exception {
        request.setExtra(callOptions.getAttributes());

        ResponseFuture<Object> responseFuture = new ResponseFuture<>(eventExecutor);

        // 为了准确控制超时，需要有线程单独执行发送，避免因为连接、初始化等导致超时时间和预设不一致
        Future<?> requestFuture = executorService.submit(() -> {
            try {
                invoker.sendRequest(request, callOptions, responseFuture);
            } catch (Exception e) {
                responseFuture.setFailure(e);
            }
        });
        try {
            return invoker.waitResponse(responseFuture, callOptions.getTimeout());
        } catch (Exception e) {
            requestFuture.cancel(true);
            throw e;
        }
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
