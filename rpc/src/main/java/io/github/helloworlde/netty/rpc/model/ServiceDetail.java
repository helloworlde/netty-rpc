package io.github.helloworlde.netty.rpc.model;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;

@Data
@Builder
public class ServiceDetail<T> {
    private T service;

    private T instance;

    private Map<String, Method> methods;
}
