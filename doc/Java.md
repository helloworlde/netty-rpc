## Java 

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
Server server = ServerBuilder.builder()
                             .port(9096)
                             .addService(HelloService.class, new HelloServiceImpl())
                             .build();

server.start();
server.awaitTermination();
```

#### 3. 客户端

```java
Client client = ClientBuilder.builder()
                             .forAddress("127.0.0.1", 9096)
                             .build();

client.start();

HelloService helloService = new ServiceProxy(client).newProxy(HelloService.class);

String response = helloService.sayHello("Netty RPC");
log.info("返回的响应结果: {}", response);

client.shutdown();
```