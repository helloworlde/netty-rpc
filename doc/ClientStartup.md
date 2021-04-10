# 客户端启动

## 构建 Client

```java
Client client = ClientBuilder.builder()
                             .forAddress("127.0.0.1", 9096)
                             .addInterceptor(new ClientInterceptorOne())
                             .build();

client.start();

HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);
```

## 初始化并启动

### 1. 初始化 Client 对象

初始化 Client 时，创建 Netty Client，并创建 TransportFactory，用于创建 Transport；为 LoadBalancer、NameResolver 设置需要的属性

```java
public Client init() {
    Bootstrap bootstrap = new Bootstrap();
    ClientHandler handler = new ClientHandler();
    workerGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("transport-io"));

    bootstrap.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new LoggingHandler(LogLevel.TRACE))
             .handler(new ClientChannelInitializer(handler));
	// 构建 TransportFactory
    TransportFactory transportFactory = new TransportFactory(bootstrap, enableHeartbeat);
	
    return this;
}
```

### 2. 启动

启动 Client 时，会启动服务发现，从注册中心获取服务端的实例，然后建立连接

```java
public void start() throws Exception {
    if (Objects.isNull(workerGroup)) {
        this.init();
    }

    this.nameResolver.setAuthority(this.authority);
    this.nameResolver.start();
}
``` 

### 3. 服务发现

以 Consul 为例，使用 Consul 作为服务注册和发现；在启动时会创建一个独立的线程池，并提交定时任务，从 Consul 中拉取相应的实例，使用获取的实例信息，更新 LoadBalancer 的实例列表，用于在请求时选择

```java
@Override
public void start() {
    super.start();
    this.executor = new ScheduledThreadPoolExecutor(2, new DefaultThreadFactory("name-resolver"));
    this.executor.scheduleAtFixedRate(this::refresh, 5, 20, TimeUnit.SECONDS);
}

@Override
public synchronized void refresh() {
	// ...
}
```

### 4. 创建服务代理

使用启动的 Client 创建服务代理；在创建时，会先创建一个 `RequestInvoker` 对象，用于创建请求，发起请求，等待响应；然后创建拦截器，默认的拦截器是 `RequestInterceptor`，在执行时会调用 `RequestInvoker` 发送请求；

该服务代理使用 Java 的 `InvocationHandler`，以反射的方式代理接口

```java
public ServiceProxy(Client client) {
    RequestInvoker invoker = new RequestInvoker(client.getLoadBalancer());
	// 创建 ClientCall 对象
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

public <T> T newProxy(Class<T> serviceClass) {
    return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{serviceClass}, this);
}
```