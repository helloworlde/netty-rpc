# 服务端启动

## 构建 Server

```java
Server server = ServerBuilder.builder()
                             .port(9090)
                             // 添加服务
                             .addService(HelloService.class, new HelloServiceImpl())
	                         // 添加拦截器
                             .addInterceptor(new ServerInterceptorOne())
							 // 构建 Server 实例
                             .build();
// 启动
server.start();
server.awaitTermination();
```

## 初始化并启动

服务端启动时，会先向服务注册器中添加需要注册的服务；然后初始化拦截器和请求处理器；最后创建并初始化 Transport，用于处理请求；

当初始化完成后会执行监听端口，随后将服务注册到注册中心


### 1. 添加服务

将服务和相应的实例添加到注册器中，用于在处理请求时根据服务名和方法名，从注册器中获取实例，通过反射的方式处理请求

相关的服务类，实例和方法都保存在 `ServiceDefinition`对象中

```java
public class ServiceRegistry {
	// 将服务的方法
    private final Map<String, ServiceDefinition<?>> serviceDefinitionMap = new ConcurrentHashMap<>();

    public void addService(Class<?> service, Object instance) {
        log.info("添加服务: {}, 实例: {}", service.getName(), instance.toString());
        serviceDefinitionMap.putIfAbsent(service.getName(), ServiceDefinition.builder()
                                                                             .service(service)
                                                                             .instance(instance)
                                                                             .methods(Arrays.stream(service.getMethods())
                                                                                            .collect(Collectors.toMap(Method::getName, m -> m)))
                                                                             .build());
    }
}
```

### 2. 初始化拦截器

初始化请求拦截器，然后对其他的拦截器排序并初始化；

```java
// 初始化请求拦截器
HandlerInterceptor handlerInterceptor = new HandlerInterceptor(serviceRegistry);
ServerCall serverCall = new ServerCall(handlerInterceptor);

if (Objects.nonNull(this.interceptors)) {
	// 对拦截器排序
    interceptors = this.interceptors.stream()
                                    .sorted(Comparator.comparing(ServerInterceptor::getOrder))
                                    .collect(Collectors.toList());
	// 创建拦截器对象链
    for (ServerInterceptor interceptor : interceptors) {
        serverCall = new ServerCall(serverCall, interceptor);
    }
}
```

### 3. 初始化请求处理器

```java
// 请求处理器
RequestProcessor requestProcessor = new RequestProcessor(serverCall);
```

### 4. 创建并初始化 Transport

- 创建 Transport

```java
// 初始化 Transport
transport = new Transport();
transport.doInit(requestProcessor);
```

- 初始化 Transport

初始化时，创建 Netty 服务器

```java
public synchronized void doInit(RequestProcessor requestProcessor) 
    bossGroup = new NioEventLoopGroup(4, new DefaultThreadFactory("accept-group"));
    workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("io-event-group"));
    executor = new ThreadPoolExecutor(10, 100, 60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new DefaultThreadFactory("business-group"));

    serverBootstrap = new ServerBootstrap();
    serverBootstrap.group(bossGroup, workerGroup)
                   .channel(NioServerSocketChannel.class)
                   .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                   .handler(new LoggingHandler(LogLevel.DEBUG))
                   .childHandler(new ServerChannelInitializer(requestProcessor, executor));
}
``` 

- Netty Channel  初始化

Netty 初始化时，添加了基于属性长度的解码器、请求编解码器和请求处理器，用于处理请求逻辑

```java
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          .addLast(new MessageDecoder<>(Request.class))
          .addLast(new MessageEncoder())
          .addLast(new ServerHandler(this.processor, this.executor));
    }
}
```

### 5. 启动服务端

- 启动

```java
public void start() throws InterruptedException {
	// 如果还没有初始化，则先初始化
    if (Objects.isNull(transport)) {
        this.init();
    }
	// 绑定端口
    this.port = transport.doBind(this.port);
    // 注册到注册中心
    this.registry.register(this.name, this.address, this.port, this.metadata);
}
```

- 绑定监听端口

绑定要要监听的端口，如果当前端口不可用，则绑定下一个端口，直到绑定成功

绑定端口成功后，即可接收处理请求

```java
public int doBind(int port) throws InterruptedException {
    ChannelFuture channelFuture = serverBootstrap.bind(port);

    channelFuture.await();
    if (!channelFuture.isSuccess()) {
        log.error("Server 绑定端口: {} 失败，尝试其他端口", port);
        return doBind(port + 1);
    }
    this.channel = channelFuture.channel();
    return port;
}
``` 
