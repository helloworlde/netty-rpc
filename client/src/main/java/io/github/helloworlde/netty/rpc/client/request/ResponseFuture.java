package io.github.helloworlde.netty.rpc.client.request;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ResponseFuture<V> extends DefaultPromise<V> {

    public ResponseFuture() {
    }

    public ResponseFuture(EventExecutor executor) {
        super(executor);
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return super.get(timeout, unit);
        } catch (TimeoutException e) {
            throw new TimeoutException(String.format("请求超时，超时时间: %d", timeout));
        }
    }
}
