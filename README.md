# Netty RPC

> 基于 Netty 手动实现一个 Java RPC 框架

## 快速使用

### Spring Boot 

#### 1. 引入依赖

```kotlin
val nettyRpcVersion = "0.0.1-SNAPSHOT"

repositories {
    maven {
        setUrl("https://maven.pkg.github.com/helloworlde/netty-rpc")
    }
}

dependencies {
    // 客户端
    implementation("io.github.helloworlde:netty-rpc-spring-boot-starter-client:${nettyRpcVersion}")
    // 服务端
    implementation("io.github.helloworlde:netty-rpc-spring-boot-starter-server:${nettyRpcVersion}")
}
```

#### 2. 服务端

- 实现服务

```java
@NettyRpcService
@Slf4j
public class HelloServiceImpl implements HelloService {
    // ...
}
```

#### 3. 客户端

- 接口调用

```java
@RestController
public class ExampleController {

    @NettyRpcClient("netty-rpc-server")
    private HelloService helloService;
    
    // ...
}
```

### Java 

#### 1. 引入依赖

```groovy
val nettyRpcVersion = "0.0.1-SNAPSHOT"

repositories {
    maven {
        setUrl("https://maven.pkg.github.com/helloworlde/netty-rpc")
    }
}

dependencies {
    // 客户端
    implementation("io.github.helloworlde:netty-rpc-client:${nettyRpcVersion}")
    // 服务端
    implementation("io.github.helloworlde:netty-rpc-server:${nettyRpcVersion}")
}
```

#### 2. 服务端

```java
Server server = Server.server()
                      .port(9096)
                      .addService(HelloService.class, new HelloServiceImpl());

server.start();
server.awaitTermination();
```

#### 3. 客户端

```java
Client client = Client.client()
                      .forAddress("127.0.0.1", 9096)
                      .start();

HelloService helloService = new ServiceProxy<HelloService>(client).newProxy(HelloService.class);

String response = helloService.sayHello("Netty RPC");

client.shutdown();
```

## 协议

使用自定义协议，格式为：`MagicNumber + Serialize + Length + Body`

- `MagicNumber` 为 `0x1024`，是一个 int 值，长度为 4 个字节
- `Serialize` 表示序列化方式，默认使用 JSON 协议，是一个 int 值，长度为 4 个字节
- `Length` 表示 `Body` 的长度，是一个 int 值，长度为 4 个字节
- `Body` 表示请求的具体内容，长度为 `Length` 个字节
