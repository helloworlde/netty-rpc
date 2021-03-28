# Netty RPC

> 基于 Netty 手动实现一个 Java RPC 框架

## 快速使用

1. 引入依赖

```groovy

```

2. 实现服务端

```java
public class HelloworldServer {

    public static void main(String[] args) throws InterruptedException {
        Server server = Server.server()
                              .port(9096)
                              .addService(HelloService.class, new HelloServiceImpl());

        server.start();
        server.awaitTermination();
    }
}

@Slf4j
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String message) {
        log.info("新的请求: {}", message);
        return "Hello " + message;
    }
}
```

3. 实现客户端

```java
public class HelloworldClient {

    public static void main(String[] args) throws Exception {
        Client client = new Client()
                .forAddress("127.0.0.1", 9096)
                .start();

        HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

        String response = helloService.sayHello("Netty RPC");
        log.info("返回的响应结果: {}", response);

        client.shutdown();
    }
}
```

## 协议

使用自定义协议，格式为：

`MagicNumber + Serialize + Length + Body`

- `MagicNumber` 为 `0x1024`，是一个 int 值，长度为 4 个字节
- `Serialize` 表示协议，默认使用 JSON 协议，是一个 int 值，长度为 4 个字节
- `Length` 表示 `Body` 的长度，是一个 int 值，长度为 4 个字节
- `Body` 表示请求的具体内容，长度为 `Length` 个字节
