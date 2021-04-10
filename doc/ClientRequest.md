# 客户端请求流程

## 实现

### 1. 服务代理

服务代理通过 Java 反射实现，在 Client 启动后，使用 Client 和服务类生成代理对象，作为服务在客户端的实例，用于执行请求

- 构建代理类实例

创建用于执行请求调用的 `RequestInvoker`,`ClientCall` 等对象的实例，初始化拦截器

```java
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
```

- 创建被代理类实例

```java
public <T> T newProxy(Class<T> serviceClass) {
    return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{serviceClass}, this);
}
```

### 2. 调用方法

调用方法时，会首先执行被代理的实例的 invoke 方法，在这个方法中，会创建请求，执行调用

- 发起调用

```java
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
```

- 发送请求

通过 `ClientCall#call` 方法调用会执行所有的拦截器的逻辑，最后一个拦截器是默认的请求拦截器 `RequestInterceptor`，会在这个拦截器中发送请求；通过 Future 阻塞获取响应结果

```java
public Object interceptorCall(Request request, CallOptions callOptions, ClientCall next) throws Exception {
    request.setExtra(callOptions.getAttributes());
    ResponseFuture<Object> responseFuture = new ResponseFuture<>();
    invoker.sendRequest(request, callOptions, responseFuture);
    return invoker.waitResponse(responseFuture, callOptions.getTimeout());
}
```

### 3. 发送请求

- 选择服务端节点

在发送请求时，会先使用负载均衡策略，选择要请求的服务端实例，获取相应的 Transport，然后将请求写入到 Transport 中

```java
public void sendRequest(Request request, CallOptions callOptions, ResponseFuture<Object> responseFuture) throws Exception {
    Transport transport = loadBalancer.chooseTransport();
    transport.write(request, responseFuture);
}
```

- 写入请求

写入请求是通过 Netty 的 Channel 实现的，会先将请求 ID 添加到集合中，用于收到响应后写入等待的 Future 中；然后会将请求对象写入 Channel 并发送，由 Netty 编码并将请求内容发送给服务端

```java
public void write(Request request, ResponseFuture<Object> responseFuture) {
    log.debug("请求 {} Channel: {}", request.getRequestId(), channel);
    this.paddingRequests.putIfAbsent(request.getRequestId(), responseFuture);
    channel.writeAndFlush(request);
}
```

- 编码请求

写入 Netty 的对象在发送前会先编码，在编码时会根据协议，写入协议、序列化类型、请求内容的长度和具体内容，完成编码后，写入到 ByteBuf 中，发送给服务端

```java
protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    out.writeInt(Constants.PROTOCOL_MAGIC);

    // 序列化类型
    Serialize serialize = SerializeEnum.JSON.getSerialize();
    out.writeInt(SerializeEnum.JSON.getId());

    // Body
    byte[] requestBody = serialize.serialize(msg);
    out.writeInt(requestBody.length);
    out.writeBytes(requestBody);
}
```

### 4. 等待响应

当服务端完成请求，并返回响应结果后，会由 Netty 处理；解码后由对应的处理器处理

- 解码响应

解码时同样会读取协议、序列化类型、响应长度和响应内容，并反序列化为具体的对象

```java
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

    int protocol = in.readInt();
    if (Constants.PROTOCOL_MAGIC != protocol) {
        log.warn("协议无法识别: {}", protocol);
        ctx.close();
    }

    // 序列化类型
    int serializeType = in.readInt();
    Serialize serialize = SerializeEnum.getById(serializeType);

    // Body
    int length = in.readInt();
    byte[] bodyBytes = new byte[length];
    in.readBytes(bodyBytes);

    T result = serialize.deserialize(bodyBytes, decodeClass);
    out.add(result);
}
```

- 处理响应

由自定义实现的 Netty 的 Handler 处理响应，从响应中获取请求的 ID，并将响应内容添加到等待的 Future 中

```java
protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
    log.debug("接收到响应: {}", msg.getRequestId());
    Long requestId = msg.getRequestId();
    try {
        receiveResponse(msg);
    } catch (Exception e) {
        receiveError(requestId, e);
    } finally {
        completeRequest(requestId);
    }
}
```

- 将响应添加到 Future 中

```java
public void receiveResponse(Response msg) {
    if (msg.getError() == null) {
        paddingRequests.get(msg.getRequestId())
                       .setSuccess(msg.getBody());
    } else {
        receiveError(msg.getRequestId(), new RpcException("Response failed: " + msg.getError()));
    }
}
```

- 返回响应结果

将响应结果返回给调用的方法，完成请求

```java
public Object waitResponse(ResponseFuture<Object> future, Long timeout) throws Exception {
    if (timeout <= 0) {
        return future.get();
    } else {
        return future.get(timeout, TimeUnit.MILLISECONDS);
    }
}
```