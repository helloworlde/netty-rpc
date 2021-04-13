# SPI 加载 LoadBalancer

Java 提供了 SPI(Service Provider Interface)机制，解耦实现类和接口，可以用于扩展第三方实现或替换组件；在 Netty RPC 中使用 SPI 机制加载 LoadBalancer，方便扩展和自定义实现

## SPI 使用

SPI 支持加载接口或者抽象类，需要在 `resources/META-INF/services` 目录下添加名为接口或者抽象类限定名的文件，里面的内容是需要加载的实现类的限定类名

### 加载

```java
ServiceLoader.load(LoadBalancer.class);
```

- java.util.ServiceLoader#reload

加载时使用 `ServiceLoader` 和类名作为参数，调用 `ServiceLoader#reload`方法，创建一个 `LazyIterator`实例

```java
private ServiceLoader(Class<S> svc, ClassLoader cl) {
    loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
    reload();
}

public void reload() {
    providers.clear();
    lookupIterator = new LazyIterator(service, loader);
}
```

- java.util.ServiceLoader.LazyIterator#hasNextService

会在真正使用时通过调用 `LazyIterator#hasNextService`  方法，读取文件，解析后读取并加载所有的类

```java
private boolean hasNextService() {
	// 加载 resources/META-INF/services/限定类名 文件
    String fullName = PREFIX + service.getName();
    configs = loader.getResources(fullName);
    while ((pending == null) || !pending.hasNext()) {
        if (!configs.hasMoreElements()) {
            return false;
        }
        // 从文件内容中读取要加载的类的名称
        pending = parse(service, configs.nextElement());
    }
    // 调用 nextService 方法加载
    nextName = pending.next();
    return true;
}
```

- java.util.ServiceLoader.LazyIterator#nextService

通过 `Class#forName` 方法加载具体的类，添加到集合中，完成加载

```java
private S nextService() {
    String cn = nextName;
    nextName = null;
    Class<?> c = null;
	// 根据名称加载相应类，并创建相应的实例，添加到集合中
    c = Class.forName(cn, false, loader);
    S p = service.cast(c.newInstance());
    providers.put(cn, p);
    return p;
}
```


## 实现

### 1. 添加配置文件

在 `resources/META-INF/services` 下添加名为 `io.github.helloworlde.netty.rpc.client.lb.LoadBalancer`的文件，内容是实现类

```java
io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer
io.github.helloworlde.netty.rpc.client.lb.RandomLoadBalancer
```

### 2. 加载

在类初始化时完成对 `ServiceLoader` 的初始化，将相关的类添加到集合中，用于通过名称获取

```java
public class LoadBalancerProvider {

    private static final Map<String, LoadBalancer> registry = new ConcurrentHashMap<>();

	// 在类加载时初始化
    static {
        ServiceLoader<LoadBalancer> loadBalancers = ServiceLoader.load(LoadBalancer.class);
        loadBalancers.forEach(loadBalancer -> {
            log.info("加载 LoadBalancer 策略: {}", loadBalancer.getName());
            registry.put(loadBalancer.getName(), loadBalancer);
        });
    }

    public static LoadBalancer getLoadBalancer(String name) {
        return registry.get(name);
    }
}
```

### 3. 使用

在客户端初始化时，指定 LoadBalancer 的名称，用于获取相应的实例

- 指定名称

```java
Client client = ClientBuilder.builder()
                             .loadBalancer("round_robin")
                             .build();
```

- 根据名称获取实例

```java
LoadBalancer loadBalancer = LoadBalancerProvider.getLoadBalancer(this.loadBalancerName);
```

## 参考文档

- 项目地址 [https://github.com/helloworlde/netty-rpc](https://github.com/helloworlde/netty-rpc)