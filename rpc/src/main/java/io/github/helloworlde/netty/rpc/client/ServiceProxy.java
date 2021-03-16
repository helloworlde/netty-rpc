package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.handler.Transport;
import io.github.helloworlde.netty.rpc.model.Request;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicLong;

public class ServiceProxy<T> implements InvocationHandler {

    private final AtomicLong requestSeq = new AtomicLong();
    private Transport transport;

    public ServiceProxy(Transport transport) {
        this.transport = transport;
    }

    public T newProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{serviceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> proxyClass = method.getDeclaringClass();
        String methodName = method.getName();

        Request request = createRequest(proxyClass, methodName, args);

        ResponseFuture<Object> future = transport.sendRequest(request);
        return future.get();
    }

    private Request createRequest(Class<?> proxyClass, String methodName, Object[] params) throws Exception {
        return Request.builder()
                      .requestId(requestSeq.getAndIncrement())
                      .serviceName(proxyClass.getName())
                      .methodName(methodName)
                      .params(params)
                      .build();
    }

}
