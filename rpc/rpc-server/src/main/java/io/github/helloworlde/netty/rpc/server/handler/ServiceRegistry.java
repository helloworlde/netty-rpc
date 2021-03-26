package io.github.helloworlde.netty.rpc.server.handler;

import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.ServiceDefinition;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ServiceRegistry {

    private final Map<String, ServiceDefinition<?>> serviceDefinitionMap = new ConcurrentHashMap<>();

    public void addService(Class<?> service, Object instance) {
        log.info("添加服务: {}, 实例: {}", service.getName(), instance.toString());
        serviceDefinitionMap.putIfAbsent(service.getName(), ServiceDefinition.builder()
                                                                             .service(service)
                                                                             .instance(instance)
                                                                             .methods(Arrays.stream(service.getMethods())
                                                                                            .collect(Collectors.toMap(Method::getName, m -> m)))
                                                                             .build());
    }

    public ServiceDefinition<?> getService(String serviceName) {
        return Optional.ofNullable(serviceDefinitionMap.get(serviceName))
                       .orElseThrow(() -> new RpcException("Service Not Found"));
    }
}
