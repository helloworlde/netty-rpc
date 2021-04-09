# 服务端请求处理

当接收到客户端的请求后，会由 Netty 处理；经过拆包、解码后请求由自定义的  `SimpleChannelInboundHandler`处理；从请求中获取具体的服务和方法，在服务注册器中查找对应的实例，通过反射调用获取响应结果，写入到 `Channel` 中，发送给客户端

## 实现

### 1. 拆包

首先使用 `LengthFieldBasedFrameDecoder` 进行拆包的处理，然后将请求内容向下传递，进行解码

### 2.  解码

解码由自定义的解码器实现，解码时会先校验协议是否可识别，如果可以识别，则获取序列化协议，将请求内容反序列化为请求对象，向后传递，由 `ServerHandler` 处理

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

    // 获取长度，读取相应长度的字节内容
    int length = in.readInt();
    byte[] bodyBytes = new byte[length];
    in.readBytes(bodyBytes);
	// 反序列化为请求对象
    T result = serialize.deserialize(bodyBytes, decodeClass);
    out.add(result);
}
```

### 3. 提交处理请求任务

将 decode 生成的请求提交给处理业务的线程池异步处理，避免阻塞 IO 线程

```java
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        log.debug("接收到新的请求: {}", request.getRequestId());

        executor.execute(() -> this.processor.process(channel, request));
    }
}    
```

### 4. 处理请求

请求在 `RequestProcessor` 中处理，调用 `ServerCall#call` 执行拦截器的逻辑，由最后一个拦截器处理方法调用；当返回结果后将结果添加到响应中，通过 `Channel`发送给客户端，完成请求

```java
public class RequestProcessor {
    public void process(Channel channel, Request request) {
        Response response = Response.builder()
                                    .requestId(request.getRequestId())
                                    .build();

        try {
            Object responseBody = serverCall.call(request, callOptions);
            response.setBody(responseBody);
        } catch (RpcException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            response.setError("INTERNAL ERROR");
        } finally {
            channel.writeAndFlush(response)
                   .addListener(f -> log.debug("发送响应完成"));
        }
    }
}
```

### 5. 处理方法调用

调用最终由最后一个执行的拦截器`HandlerInterceptor`处理；在处理时会根据服务和方法的名称从服务注册器中获取相应的服务实例类的实例，然后通过反射的方式调用具体的方法，执行业务逻辑，并返回响应；完成请求

```java
public class HandlerInterceptor implements ServerInterceptor {

    @Override
    public Object interceptorCall(Request request, CallOptions callOptions, ServerCall next) throws Exception {
        String serviceName = request.getServiceName();
        String methodName = request.getMethodName();

        // Service
        ServiceDefinition<?> serviceDefinition = serviceRegistry.getService(serviceName);
        Method method = serviceDefinition.getMethods().get(methodName);

        Object[] params = request.getParams();
        return method.invoke(serviceDefinition.getInstance();
    }
}
```