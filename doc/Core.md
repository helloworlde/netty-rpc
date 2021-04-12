# 核心实现

使用 Netty 作为 RPC 的传输层，使用 Netty 的相关扩展实现请求和响应的编解码、协议的封装

## 协议

使用自定义的协议，格式为 `MagicNumber + Serialize + Length + Body`
- `MagicNumber`: 表示自定义的协议类型，默认为 `0x1024`，是一个 int 值，长度为 4 个字节
- `Serialize`：表示序列化协议类型，是一个 int 值，长度为 4 个字节
- `Length`：表示 `Body` 的长度，用于读取完整的 `Body` 内容
- `Body` ：请求或响应的内容，长度为 `Length`

即一个请求或响应的长度为：`4 + 4 + Length`；为了解决 Netty 粘包的问题，使用 `LengthFieldBasedFrameDecoder` 处理收到的内容

## 编解码

### 编码

请求的编码相对简单，在编码时依次写入协议、序列化类型、Body 长度和 Body 内容

```java
protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
	// 协议
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

### 解码

在解码时，先读取内容校验是否是可以识别的自定义协议；如果不是则忽略；如果是可以识别的协议，则读取序列化类型并初始化；接着读取请求的长度和具体内容，将请求内容反序列化为对象

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

## 服务端

### 1. 创建 Netty 服务端

创建 Netty 服务端

```java
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
```


### 2. Channel 初始化

初始化时添加了基于长度的解码器，自定义的编解码器和处理请求逻辑的处理器

```java
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // lengthFiledOffset 请求内容的偏移量；MagicNumber + Serialize = 8
        // lengthFieldLength 请求内容的长度标识偏移量 Length = 4
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          .addLast(new MessageDecoder<>(Request.class))
          .addLast(new MessageEncoder())
          .addLast(new ServerHandler(this.processor, this.executor));
    }
}
```

### 3. ServerHandler

用于执行请求的处理逻辑，并发送响应给客户端

```java
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
	     // 请求处理逻辑
    }
}    
```

## 客户端

### 1. 创建 Netty 客户端

创建 Netty 客户端

```java
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
```

### 2. Channel 初始化

初始化时同样添加了基于长度的解码器，自定义的编解码器和处理请求逻辑的处理器

```java
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // lengthFiledOffset 请求内容的偏移量；MagicNumber + Serialize = 8
        // lengthFieldLength 请求内容的长度标识偏移量 Length = 4
        ch.pipeline()
          .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 8, 4))
          .addLast(new MessageEncoder())
          .addLast(new MessageDecoder<>(Response.class))
          .addLast(handler);
    }
}
```

### 3.  ClientHandler

执行发送请求和接收响应的具体逻辑

```java
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Response> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        // 处理响应
    }

    public void write(Request request, ResponseFuture<Object> responseFuture) {
        channel.writeAndFlush(request);
    }
}    
```