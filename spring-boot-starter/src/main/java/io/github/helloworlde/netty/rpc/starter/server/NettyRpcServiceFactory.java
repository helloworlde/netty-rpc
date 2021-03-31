package io.github.helloworlde.netty.rpc.starter.server;

import io.github.helloworlde.netty.rpc.registry.ConsulRegistry;
import io.github.helloworlde.netty.rpc.server.Server;
import io.github.helloworlde.netty.rpc.server.handler.ServiceRegistry;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NettyRpcServiceFactory implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;

    private Environment environment;

    public NettyRpcServiceFactory(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        assert beanFactory instanceof DefaultListableBeanFactory;
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public Server createServer() {

        List<String> beanNames = Arrays.asList(this.beanFactory.getBeanNamesForAnnotation(NettyRpcService.class));

        ServiceRegistry registry = new ServiceRegistry();

        beanNames.stream()
                 .map(name -> this.beanFactory.getBean(name))
                 .collect(Collectors.toMap(this::getInterface, b -> b))
                 .forEach(registry::addService);

        Map<String, String> metadata = new HashMap<>();

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Server.class);

        MutablePropertyValues properties = new MutablePropertyValues();
        properties.add("port", 9090);
        properties.add("serviceRegistry", registry);
        properties.add("address", "172.30.78.154");
        properties.add("name", environment.getProperty("spring.application.name"));
        properties.add("registry", new ConsulRegistry("127.0.0.1", 8500));
        properties.add("metadata", metadata);
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
