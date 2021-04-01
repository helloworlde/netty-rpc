package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class NettyRpcServiceProxyProcessor implements BeanPostProcessor {

    private final NettyRpcClientFactory clientFactory;

    private final Map<String, Class<?>> waitProcessBeanMap = new ConcurrentHashMap<>();

    public NettyRpcServiceProxyProcessor(NettyRpcClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        Stream.of(beanClass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(NettyRpcClient.class))
              .forEach(field -> waitProcessBeanMap.putIfAbsent(beanName, beanClass));

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Optional.ofNullable(waitProcessBeanMap.get(beanName))
                .map(Class::getDeclaredFields)
                .map(Stream::of)
                .ifPresent(stream -> stream.filter(f -> f.isAnnotationPresent(NettyRpcClient.class))
                                           .forEach(field -> createServiceBean(bean, field)));

        return bean;
    }

    private void createServiceBean(Object bean, Field field) {
        try {
            NettyRpcClient annotation = field.getAnnotation(NettyRpcClient.class);
            Object serviceProxy = createServiceProxy(field.getType(), annotation.value());

            field.setAccessible(true);
            field.set(bean, serviceProxy);
        } catch (IllegalAccessException e) {
            log.error("初始化客户端失败: {}", e.getMessage(), e);
        }
    }

    private Object createServiceProxy(Class<?> serviceClass, String authority) {
        Client client = clientFactory.getClient(authority);
        return new ServiceProxy(client).newProxy(serviceClass);
    }
}
