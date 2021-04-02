package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

@Slf4j
public class NettyRpcClientFactory implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;

    private final NameResolver nameResolver;

    private final Registry registry;

    private final LoadBalancer loadBalancer;

    public NettyRpcClientFactory(Registry registry,
                                 NameResolver nameResolver,
                                 LoadBalancer loadBalancer) {
        this.registry = registry;
        this.nameResolver = nameResolver;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        assert beanFactory instanceof DefaultListableBeanFactory;
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public Client getClient(String name) {
        Client client;
        try {
            client = beanFactory.getBean(name, Client.class);
        } catch (BeansException e) {
            log.info("Client for authority '{}' not exist, create a new one", name);
            client = initClient(name);
        }
        return client;
    }

    private Client initClient(String name) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Client.class);

        MutablePropertyValues properties = new MutablePropertyValues();
        properties.add("authority", name);
        properties.add("nameResolver", this.nameResolver);
        properties.add("registry", this.registry);
        properties.add("loadBalancer", loadBalancer);

        beanDefinition.setPropertyValues(properties);

        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("shutdown");

        beanFactory.registerBeanDefinition(name, beanDefinition);

        return beanFactory.getBean(name, Client.class);
    }
}
