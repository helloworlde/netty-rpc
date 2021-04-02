## Spring Boot 

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