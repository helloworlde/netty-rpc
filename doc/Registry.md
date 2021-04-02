## 服务注册

默认使用 `NoopRegistry`，不会执行注册；提供使用 Consul 的服务注册

### 使用

#### Spring Boot 

注入 `ConsulRegistry` Bean 即可

```java
@Bean
public ConsulRegistry consulRegistry() {
    return new ConsulRegistry(consulHost, consulPort);
}
```

#### Java 

指定 `ConsulRegistry`

```java
Server server = ServerBuilder.builder()
                             .port(9096)
                             .registry(new ConsulRegistry("127.0.0.1", 8500))
                             .address("172.30.78.154")
                             .addMetadata("version", "0.1")
                             .name("netty-rpc-server")
                             .build();
```

### 自定义

实现 `NameResolver` 抽象类即可

```java
public class CustomNameResolver extends NameResolver{
    // ...
}
```