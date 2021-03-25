package io.github.helloworlde.netty.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDefinition<T> {

    private T service;

    private T instance;

    private Map<String, Method> methods;
}
