# 客户端的 Spring Boot Starter

Spring Boot Starter 可以很大程度上简化配置，避免重复的手动开发相同的功能；为 Netty RPC 的 Client 开发一个 Spring Boot Starter

## 实现

### 1. 添加依赖

需要添加 Spring Boot 和 Spring Cloud 的相关依赖，用于自动配置和相关 Bean 的注入

```gradle
val springVersion = "2.4.4"
val springCloudDependenciesVersion = "2020.0.2"

dependencies {
    // 依赖管理
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudDependenciesVersion}"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springVersion}"))

    compile(project(":server"))

    // 自动配置
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    // 通用依赖
    implementation("org.springframework.cloud:spring-cloud-commons")
    // 生成配置提示
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### 2. 构建 Client 及服务代理

#### 初始化 Client

构建 Client 实例的过程和构建 Server 实例的过程类似，不同的是会构建多个 Client 对象，设置属性后注册到容器中

```java
@Slf4j
public class NettyRpcClientFactory implements BeanFactoryAware {
    // 获取或初始化 Client 对象
    public Client getClient(String name) {
        Client client;
        try {
            client = beanFactory.getBean(name, Client.class);
        } catch (BeansException e) {
            log.debug("Client for authority '{}' not exist, create a new one", name);
            client = initClient(name);
        }
        return client;
    }
    
	// 初始化 Client Bean
    private Client initClient(String name) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Client.class);

        MutablePropertyValues properties = new MutablePropertyValues();
        properties.add("authority", name);
        properties.add("nameResolver", this.nameResolver);
        properties.add("registry", this.registry);
        properties.add("loadBalancer", loadBalancer);
        properties.add("interceptors", interceptors);
        properties.add("enableHeartbeat", clientProperties.isEnableHeartbeat());

        beanDefinition.setPropertyValues(properties);

        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("shutdown");

        beanFactory.registerBeanDefinition(name, beanDefinition);

        return beanFactory.getBean(name, Client.class);
    }
}
```

#### 初始化服务代理

- NettyRpcClient

使用自定义的注解标记服务代理，用于为服务生成代理类，并将相关的 Bean 注册到容器中

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyRpcClient {
    String value();
}
```

- NettyRpcServiceProxyProcessor

`NettyRpcServiceProxyProcessor` 实现了 `BeanPostProcessor`，在 Bean 处理完成之前，查找 Bean 中有 `NettyRpcClient`  注解的属性，放入到集合中；当 Bean 处理完成之后，为这些属性创建服务代理对象并赋值

```java
public class NettyRpcServiceProxyProcessor implements BeanPostProcessor {

    private final NettyRpcClientFactory clientFactory;

    private final Map<String, Class<?>> waitProcessBeanMap = new ConcurrentHashMap<>();

    public NettyRpcServiceProxyProcessor(NettyRpcClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
		// 查找所有的包含 NettyRpcClient 注解属性的 Bean，添加到待处理的集合中
        Stream.of(beanClass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(NettyRpcClient.class))
              .forEach(field -> waitProcessBeanMap.putIfAbsent(beanName, beanClass));

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
	    // 在 Bean 处理完成后，遍历所有待处理的 Bean，为属性创建服务代理
        Optional.ofNullable(waitProcessBeanMap.get(beanName))
                .map(Class::getDeclaredFields)
                .map(Stream::of)
                .ifPresent(stream -> stream.filter(f -> f.isAnnotationPresent(NettyRpcClient.class))
                                           .forEach(field -> createServiceBean(bean, field)));

        return bean;
    }

    private void createServiceBean(Object bean, Field field) {
        try {
	        // 创建服务代理，并为属性赋值
            NettyRpcClient annotation = field.getAnnotation(NettyRpcClient.class);
            Object serviceProxy = createServiceProxy(field.getType(), annotation.value());

            field.setAccessible(true);
            field.set(bean, serviceProxy);
        } catch (IllegalAccessException e) {
            log.error("初始化客户端失败: {}", e.getMessage(), e);
        }
    }

    private Object createServiceProxy(Class<?> serviceClass, String authority) {
	    // 获取 Client 对象，创建服务代理
        Client client = clientFactory.getClient(authority);
        return new ServiceProxy(client).newProxy(serviceClass);
    }
}
```

### 3. Client 与服务生命周期绑定

通过 `SmartLifeCycle` 创建并初始化所有的 Client，确保在 Tomcat 启动后再初始化，避免在依赖还没有就绪时过早初始化

```java
public class NettyRpcClientSmartLifecycle implements SmartLifecycle {

    @Override
    public void start() {
        log.debug("开始启动 Client");
        this.clients = context.getBeansOfType(Client.class);

        clients.forEach((name, client) -> {
            try {
                client.start();
            } catch (Exception e) {
                log.error("启动 Client: {} 失败: {}", name, e.getMessage(), e);
            }
        });
    }
}

```

### 4. 自动配置

- NettyRpcClientAutoConfiguration

添加 NettyRpcClient 的自动配置，注入 `NettyRpcClientFactory`，`NettyRpcServiceProxyProcessor` 和 `NettyRpcClientSmartLifecycle` 的 Bean

```java
@Configuration
@EnableConfigurationProperties(ClientProperties.class)
@ConditionalOnNettyRpcClientEnabled
public class NettyRpcClientAutoConfiguration {

    @Bean
    public NettyRpcClientFactory rpcClientFactory(ClientProperties clientProperties,
                                                  @Nullable Registry registry,
                                                  @Nullable NameResolver nameResolver,
                                                  @Nullable LoadBalancer loadBalancer,
                                                  @Nullable ClientInterceptor[] interceptors) {

        return new NettyRpcClientFactory(clientProperties, registry, nameResolver, loadBalancer, interceptors);
    }

    @Bean
    public NettyRpcServiceProxyProcessor rpcClientProcessor(NettyRpcClientFactory clientFactory) {
        return new NettyRpcServiceProxyProcessor(clientFactory);
    }

    @Bean
    public NettyRpcClientSmartLifecycle clientSmartLifecycle(ApplicationContext context) {
        return new NettyRpcClientSmartLifecycle(context);
    }
}    
```

- spring.factories

将 `NettyRpcClientAutoConfiguration` 添加到 `resources/META-INF/spring.factories` 中，用于 Spring Boot 在启动时自动加载

```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.github.helloworlde.netty.rpc.starter.client.NettyRpcClientAutoConfiguration
```

## 使用

- 添加依赖

```gradle
dependencies {
    implementation("io.github.helloworlde:spring-boot-starter-client")
}
```

- 为属性添加注解

```java
@RestController
public class ExampleController {

    @NettyRpcClient("netty-rpc-server")
    private HelloService helloService;

    @GetMapping("/hello")
    public Object hello(String message) {
        return helloService.sayHello(message);
    }
}
```

## 参考文档

- [github.com/helloworlde/netty-rpc](https://github.com/helloworlde/netty-rpc)