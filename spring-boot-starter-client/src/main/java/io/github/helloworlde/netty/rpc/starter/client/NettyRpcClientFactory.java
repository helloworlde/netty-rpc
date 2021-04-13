package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.nameresovler.NameResolver;
import io.github.helloworlde.netty.rpc.interceptor.ClientInterceptor;
import io.github.helloworlde.netty.rpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class NettyRpcClientFactory implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;

    private final NameResolver nameResolver;

    private final Registry registry;

    private List<ClientInterceptor> interceptors;

    private ClientProperties clientProperties;

    public NettyRpcClientFactory(ClientProperties clientProperties,
                                 Registry registry,
                                 NameResolver nameResolver,
                                 ClientInterceptor[] interceptors) {
        this.clientProperties = clientProperties;
        this.registry = registry;
        this.nameResolver = nameResolver;
        if (Objects.nonNull(interceptors)) {
            this.interceptors = Arrays.asList(interceptors);
        }
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
            log.debug("Client for authority '{}' not exist, create a new one", name);
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
        properties.add("interceptors", interceptors);
        properties.add("enableHeartbeat", clientProperties.isEnableHeartbeat());
        properties.add("timeout", clientProperties.getTimeout());
        properties.add("serializeName", clientProperties.getSerializeName());
        properties.add("loadBalancerName", clientProperties.getLoadBalancerName());

        beanDefinition.setPropertyValues(properties);

        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("shutdown");

        beanFactory.registerBeanDefinition(name, beanDefinition);

        return beanFactory.getBean(name, Client.class);
    }
}
