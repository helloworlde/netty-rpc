# 客户端拦截器

在使用框架的过程中，往往需要链路追踪、监控、鉴权、自定义扩展逻辑等；最常用的就是拦截器，在请求执行过程中插入逻辑；为了实现逻辑的可插拔和可扩展，使用责任链的模式设计拦截器

参考 gRPC 的拦截器，在 Netty RPC 中实现相关功能

gRPC 的拦截器实现是在初始化 `Channel` 时，使用 `Channel` 和相应的拦截器，重新构建一个 `InterceptorChannel` 对象，这个对象代理了 `Channel`
；当执行请求时，先执行拦截器的逻辑，然后创建并返回 `ClientCall` 对象，然后调用被代理的 `Channel` 对象，执行请求；当有多个拦截器时，多次执行该逻辑

## 实现

### 定义

#### 1. 拦截器接口

定义拦截器接口，有两个方法：

- `interceptorCall`：用于实现具体的拦截器逻辑
- `getOrder`：用于控制拦截器的执行顺序，值越大优先级越高，越先执行

```java
public interface ClientInterceptor {

    Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception;

    Integer getOrder();
}
```

#### 2. 调用对象

定义 `ClientCall` 对象，用于执行请求调用逻辑；有两个属性：

- `interceptor`：这个 `ClientCall` 持有的拦截器对象，用于在调用 call 方法时调用拦截器，执行相关逻辑
- `next`：被代理的 `ClientCall` 对象，即下一个要调用的 `ClientCall`

```java
public class ClientCall {

    private final ClientInterceptor interceptor;

    private final ClientCall next;

    public ClientCall(ClientInterceptor interceptor) {
        this.next = null;
        this.interceptor = interceptor;
    }

    public ClientCall(ClientCall next, ClientInterceptor interceptor) {
        this.next = next;
        this.interceptor = interceptor;
    }

    public Object call(Request request, CallOptions callOptions) throws Exception {
        return interceptor.interceptorCall(request, callOptions, next);
    }

}
```

### 实现

#### 1. 实现发送请求拦截器

这是一个默认的拦截器，实现了发送请求的逻辑，在客户端中，这是优先级最低的拦截器，即最后一个执行的拦截器；在服务启动时自动添加

在执行时，请求通过 `RequestInvoker` 发送，等待响应结果并返回给上一个拦截器

```java
public class RequestInterceptor implements ClientInterceptor {

    private final RequestInvoker invoker;

    public RequestInterceptor(RequestInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        // 调用发送请求逻辑
        return sendRequest(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return Integer.MIN_VALUE;
    }
}
```

#### 2. 初始化客户端服务代理

在初始化 `ServiceProxy` 时，先创建请求拦截器对象，然后使用这个拦截器创建默认的 `ClientCall` 对象；然后获取所有的拦截器，排序后依次初始化

最终，每个拦截器的 `ClientCall` 中持有自己的拦截器和下一个要被调用的 `ClienCall` 对象；`RequestInterceptor` 这个拦截器的下一个 `ClientCall`是 null

在发起请求时，调用最后一个初始化的 `ClientCall`对象的 `call`方法，在 `call` 方法中会调用拦截器的方法执行拦截器的逻辑，依次调用直到执行最后一个 `RequestInterceptor`
发送请求，等待响应返回后再依次返回结果

```java
public class ServiceProxy implements InvocationHandler {

    private final ClientCall clientCall;

    public ServiceProxy(Client client) {
        RequestInvoker invoker = new RequestInvoker(client.getLoadBalancer());
        // 初始化请求拦截器
        RequestInterceptor requestInterceptor = new RequestInterceptor(invoker);
        ClientCall tempClientCall = new ClientCall(requestInterceptor);

        List<ClientInterceptor> interceptors = client.getInterceptors();

        // 初始化拦截器
        if (Objects.nonNull(interceptors)) {
            // 对拦截器排序
            interceptors = interceptors.stream()
                                       .sorted(Comparator.comparing(ClientInterceptor::getOrder))
                                       .collect(Collectors.toList());
            // 依次初始化拦截器的 ClientCall 对象
            for (ClientInterceptor interceptor : interceptors) {
                tempClientCall = new ClientCall(tempClientCall, interceptor);
            }
        }

        this.clientCall = tempClientCall;
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
```

### 使用

#### 1. 自定义拦截器

通过实现 `ClientInterceptor` 即可实现自定义的拦截器

```java
public class CustomClientInterceptor implements ClientInterceptor {
    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
        log.info("执行 CustomClientInterceptor");
        return next.call(request, callOptions);
    }

    @Override
    public Integer getOrder() {
        return 1;
    }
}
```

#### 2. 添加到 Client 中

```java
Client client=ClientBuilder.builder()
        .forAddress("127.0.0.1",9096)
        .addInterceptor(new CustomClientInterceptor())
        .build();
```

#### 3. 调用

当执行请求时，会根据拦截器的优先级依次调用；如图，依次调用了 `ClientMetricsInterceptor`, `CustomClientInterceptor`,  `ClientTraceInterceptor`
，最终调用了 `RequestInterceptor`发送了请求

![netty-rpc-interceptor-client.png](https://hellowoodes.oss-cn-beijing.aliyuncs.com/picture/netty-rpc-interceptor-client.png)

## 参考文档

- 项目地址: [https://github.com/helloworlde/netty-rpc](https://github.com/helloworlde/netty-rpc)
- [gRPC 拦截器和监听器](https://helloworlde.github.io/2021/01/03/gRPC-%E6%8B%A6%E6%88%AA%E5%99%A8%E5%92%8C%E7%9B%91%E5%90%AC%E5%99%A8/)