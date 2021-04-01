package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.registry.Registry;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NettyRpcServiceFactory implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;

    private final ApplicationContext context;

    private final ServerProperties serverProperties;

    private final Registry registry;

    private final InetUtils inetUtils;

    public NettyRpcServiceFactory(ApplicationContext context,
                                  InetUtils inetUtils,
                                  @Nullable Registry registry,
                                  ServerProperties serverProperties) {
        this.context = context;
        this.inetUtils = inetUtils;
        this.registry = registry;
        this.serverProperties = serverProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        assert beanFactory instanceof DefaultListableBeanFactory;
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public Server createServer() {

        List<String> beanNames = Arrays.asList(this.beanFactory.getBeanNamesForAnnotation(NettyRpcService.class));

        ServiceRegistry serviceRegistry = new ServiceRegistry();

        beanNames.stream()
                 .map(name -> this.beanFactory.getBean(name))
                 .collect(Collectors.toMap(this::getInterface, b -> b))
                 .forEach(serviceRegistry::addService);

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Server.class);

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
        beanDefinition.setPropertyValues(properties);

        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("shutdown");

        beanFactory.registerBeanDefinition("nettyRpcServer", beanDefinition);
        return beanFactory.getBean("nettyRpcServer", Server.class);
    }

    private Class<?> getInterface(Object service) {
        return Arrays.stream(service.getClass().getInterfaces())
                     .filter(c -> service.getClass().getSimpleName().startsWith(c.getSimpleName()))
                     .findAny()
                     .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown service '%s' interface", service.getClass().getName())));
    }
}
