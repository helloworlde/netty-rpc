## 负载均衡

默认使用 `RoundRobinLoadBalancer`，提供了 `RandomLoadBalancer`

### 使用

#### Spring Boot 

注入 `ConsulNameResolver` Bean 即可

```java
@Bean
public LoadBalancer loadBalancer() {
    return new RoundRobinLoadBalancer();
}
```

#### Java 

指定 `RoundRobinLoadBalancer`

```java
Client client = ClientBuilder.builder()
                             .forTarget("netty-rpc-server")
                             .nameResolver(new ConsulNameResolver("127.0.0.1", 8500))
                             .loadBalancer(new RoundRobinLoadBalancer())
                             .build();
```

### 自定义

实现 `LoadBalancer` 抽象类即可

```java
public class CustomLoadBalancer extends LoadBalancer {
    // ...
}
```