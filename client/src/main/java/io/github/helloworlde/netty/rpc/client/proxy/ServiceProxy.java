package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ServiceProxy implements InvocationHandler {

    private final RequestInvoker invoker;

    public ServiceProxy(Client client) {
        this.invoker = new RequestInvoker(client.getLoadBalancer());
    }

    @SuppressWarnings("all")
    public <T> T newProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{serviceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> proxyClass = method.getDeclaringClass();
        String methodName = method.getName();
        Class<?> returnType = method.getReturnType();
        Object result = invoker.sendRequest(proxyClass, methodName, args);
        return returnType.cast(result);
    }

}
