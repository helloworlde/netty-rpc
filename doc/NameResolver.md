## 命名解析

默认使用 `FixedAddressNameResolver`，即使用固定的地址；提供使用 Consul 的命名解析

### 使用

#### Spring Boot 

注入 `ConsulNameResolver` Bean 即可

```java
@Bean
public ConsulNameResolver consulNameResolver() {
    return new ConsulNameResolver(consulHost, consulPort);
}
```

#### Java 

指定 `ConsulNameResolver`

```java
Server server = ServerBuilder.builder()
                             .registry(new ConsulRegistry("127.0.0.1", 8500))
                             .build();
```

### 自定义

实现 `Registry` 抽象类即可

```java
public class CustomRegistry extends Registry {
    // ...
}
```