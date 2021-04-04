package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.client.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ServiceProxy implements InvocationHandler {

    private ClientCall clientCall;

    public ServiceProxy(Client client) {
        RequestInvoker invoker = new RequestInvoker(client.getLoadBalancer());

        ClientCall tempClientCall = new ServiceProxyClientCall(invoker);

        List<ClientInterceptor> interceptors = client.getInterceptors();

        if (Objects.nonNull(interceptors)) {
            ClientCall tempClientCallCopy = tempClientCall;

            tempClientCall = interceptors.stream()
                                         .sorted(Comparator.comparing(ClientInterceptor::getOrder))
                                         .map(interceptor -> new ClientCall(tempClientCallCopy, interceptor))
                                         .findAny()
                                         .get();
        }

        this.clientCall = tempClientCall;
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
        // 创建请求
        Request request = RequestInvoker.createRequest(proxyClass, methodName, args);

        // 调用选项
        CallOptions callOptions = new CallOptions();

        // 服务代理
        ServiceProxyClientCall serviceProxyClientCall = (ServiceProxyClientCall) this.clientCall.newCall(request, callOptions);

        // 发起请求
        Object result = serviceProxyClientCall.sendRequest(request, callOptions);

        return returnType.cast(result);
    }

}
