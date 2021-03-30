package io.github.helloworlde.netty.rpc.starter.client;

import io.github.helloworlde.netty.rpc.client.Client;
import io.github.helloworlde.netty.rpc.client.proxy.ServiceProxy;
import io.github.helloworlde.netty.rpc.starter.annotation.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class NettyRpcServiceProxyProcessor implements BeanPostProcessor {

    private final NettyRpcClientFactory clientFactory;

    private final Map<String, Class<?>> beansToProcess = new ConcurrentHashMap<>();

    public NettyRpcServiceProxyProcessor(NettyRpcClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        Stream.of(beanClass.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(NettyRpcClient.class))
              .forEach(field -> beansToProcess.putIfAbsent(beanName, beanClass));

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            if (beansToProcess.containsKey(beanName)) {
                for (Field field : beansToProcess.get(beanName).getDeclaredFields()) {
                    NettyRpcClient annotation = field.getAnnotation(NettyRpcClient.class);
                    field.setAccessible(true);
                    Object serviceProxy = createServiceProxy(field.getType(), annotation.value());
                    field.set(bean, serviceProxy);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    private Object createServiceProxy(Class<?> serviceClass, String authority) {
        Client client = clientFactory.getClient(authority);
        return new ServiceProxy(client).newProxy(serviceClass);
    }

}
