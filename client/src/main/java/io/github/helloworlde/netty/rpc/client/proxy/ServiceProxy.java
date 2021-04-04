package io.github.helloworlde.netty.rpc.client.proxy;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.request.RequestInvoker;
import io.github.helloworlde.netty.rpc.interceptor.CallOptions;
import io.github.helloworlde.netty.rpc.interceptor.ClientCall;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class ServiceProxy implements InvocationHandler {

    private final ClientCall clientCall;

    public ServiceProxy(Client client) {
        RequestInvoker invoker = new RequestInvoker(client.getLoadBalancer());

        RequestInterceptor requestInterceptor = new RequestInterceptor(invoker);
        ClientCall tempClientCall = new ClientCall(requestInterceptor);

        List<ClientInterceptor> interceptors = client.getInterceptors();

        // 初始化拦截器
        if (Objects.nonNull(interceptors)) {
            interceptors = interceptors.stream()
                                       .sorted(Comparator.comparing(ClientInterceptor::getOrder))
                                       .collect(Collectors.toList());
            for (ClientInterceptor interceptor : interceptors) {
                tempClientCall = new ClientCall(tempClientCall, interceptor);
            }
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


        // 发起请求
        Object result = this.clientCall.call(request, callOptions);

        return returnType.cast(result);
    }

}
