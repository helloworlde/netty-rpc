package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.lb.RoundRobinLoadBalancer;
import io.github.helloworlde.netty.rpc.client.nameresovler.ConsulNameResolver;
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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        assert beanFactory instanceof DefaultListableBeanFactory;
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public Client getClient(String name) {
        String beanName = String.format("Client-%s", name);

        Client client;
        try {
            client = beanFactory.getBean(beanName, Client.class);
        } catch (BeansException e) {
            log.info("Client for authority '{}' not exist, create a new one", name);
            client = initClient(name, beanName);
        }
        return client;
    }

    private Client initClient(String name, String beanName) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(Client.class);

        MutablePropertyValues properties = new MutablePropertyValues();
        properties.add("authority", name);
        properties.add("nameResolver", new ConsulNameResolver("127.0.0.1", 8500));
        properties.add("loadBalancer", new RoundRobinLoadBalancer());

        beanDefinition.setPropertyValues(properties);

        beanDefinition.setInitMethodName("start");
        beanDefinition.setDestroyMethodName("shutdown");

        beanFactory.registerBeanDefinition(beanName, beanDefinition);

        return beanFactory.getBean(beanName, Client.class);
    }
}
