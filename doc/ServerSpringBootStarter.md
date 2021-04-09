# 服务端的 Spring Boot Starter

Spring Boot Starter 可以很大程度上简化配置，避免重复的手动开发相同的功能；为 Netty RPC 的 Server 开发一个 Spring Boot Starter

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

### 2. 构建 Server

#### 初始化服务

- NettyRpcService

使用自定义的注解标记服务，这个注解引用了 `@Service`，可以将服务的实现作为 Bean 自动注入到容器中，用于在初始化 Server 的时候获取相关的服务的 Bean

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface NettyRpcService {

}
```

#### 初始化 Server

初始化 Server 时，先查找所有有  `NettyRpcService` 注解的 Bean，获取 Bean 的服务定义接口，添加到服务注册器中；然后创建 Server 的 Bean，设置属性后将这个 Bean 注册到容器中

```java
public class NettyRpcServiceFactory implements BeanFactoryAware {

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        assert beanFactory instanceof DefaultListableBeanFactory;
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public Server createServer() {
	    // 查找所有 NettyRpcService 注解的 Bean
        List<String> beanNames = Arrays.asList(this.beanFactory.getBeanNamesForAnnotation(NettyRpcService.class));

        ServiceRegistry serviceRegistry = new ServiceRegistry();
		// 查找所有的服务接口，添加到服务注册器中
        beanNames.stream()
                 .map(name -> this.beanFactory.getBean(name))
                 .collect(Collectors.toMap(this::getInterface, b -> b))
                 .forEach(serviceRegistry::addService);

		// 创建并初始化 Server Bean
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Server.class);
		// 初始化属性
        MutablePropertyValues properties = new MutablePropertyValues();
        properties.add("serviceRegistry", serviceRegistry);
        properties.add("port", serverProperties.getPort());

        String address = Optional.ofNullable(serverProperties.getRegister().getAddress())
                                 .orElse(inetUtils.findFirstNonLoopbackAddress().getHostAddress());
        String name = Optional.ofNullable(serverProperties.getRegister().getName())
                              .orElse(context.getApplicationName());


        properties.add("name", name);
        properties.add("address", address);
        properties.add("metadata", serverProperties.getRegister().getMetadata());
        properties.add("registry", this.registry);
        properties.add("interceptors", interceptors);
        beanDefinition.setPropertyValues(properties);
		// 初始化和关闭方法
        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("shutdown");
		// 将 Server Bean 注册到容器中
        beanFactory.registerBeanDefinition("nettyRpcServer", beanDefinition);
        return beanFactory.getBean("nettyRpcServer", Server.class);
    }
}
```

### 3. Server 与服务生命周期绑定

通过 `SmartLifeCycle` 创建并初始化 Server，确保在 Tomcat 启动后再初始化，避免在依赖还没有就绪时过早初始化

```java
public class NettyRpcServerSmartLifecycle implements SmartLifecycle {

    public NettyRpcServerSmartLifecycle(NettyRpcServiceFactory factory) {
        this.factory = factory;
    }

    @Override
    public void start() {
        try {
	        // 创建并启动 Server
            this.server = this.factory.createServer();
            this.server.start();
        } catch (InterruptedException e) {
            log.error("启动失败: {}", e.getMessage(), e);
        }
    }
}
```

### 4. 自动配置

- NettyRpcServerAutoConfiguration

添加 NettyRpcServer 的自动配置，注入 `NettyRpcServiceFactory` 和 `NettyRpcServerSmartLifecycle` 的 Bean

```java
@Configuration
@EnableConfigurationProperties(ServerProperties.class)
@ConditionalOnNettyRpcServerEnabled
public class NettyRpcServerAutoConfiguration {

    @Bean
    public NettyRpcServiceFactory nettyRpcServiceFactory(ApplicationContext context,
                                                         InetUtils inetUtils,
                                                         Registry registry,
                                                         ServerProperties properties,
                                                         @Nullable ServerInterceptor[] interceptors) {
        return new NettyRpcServiceFactory(context, inetUtils, registry, properties, interceptors);
    }

    @Bean
    public NettyRpcServerSmartLifecycle nettyRpcLifecycle(NettyRpcServiceFactory factory) {
        return new NettyRpcServerSmartLifecycle(factory);
    }
}
```

- spring.factories

将 `NettyRpcServerAutoConfiguration` 添加到 `resources/META-INF/spring.factories` 中，用于 Spring Boot 在启动时自动加载

```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.github.helloworlde.netty.rpc.starter.server.NettyRpcServerAutoConfiguration
```

## 使用

- 添加依赖

```gradle
dependencies {
    implementation("io.github.helloworlde:spring-boot-starter-server")
}
```

- 为服务实现添加注解

```java
@NettyRpcService
@Slf4j
public class HelloServiceImpl implements HelloService {

    @SneakyThrows
    @Override
    public String sayHello(String message) {
        return "Hello " + message;
    }
}
```

- 启动服务

启动服务后可以看到，在 Tomcat 启动完成后初始化 Server 并添加了相关的服务

```
Tomcat started on port(s): 8081 (http) with context path ''
添加服务: io.github.helloworlde.netty.rpc.example.service.HelloService
添加服务: io.github.helloworlde.netty.rpc.service.HeartbeatService
Started NettyRpcServerApplication in 3.91 seconds (JVM running for 4.858)
```

## 参考文档

- [github.com/helloworlde/netty-rpc](https://github.com/helloworlde/netty-rpc)