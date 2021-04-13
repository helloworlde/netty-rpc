package io.github.helloworlde.netty.rpc.client.lb;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class LoadBalancerProvider {

    private static final Map<String, LoadBalancer> registry = new ConcurrentHashMap<>();

    static {
        ServiceLoader<LoadBalancer> loadBalancers = ServiceLoader.load(LoadBalancer.class);
        loadBalancers.forEach(loadBalancer -> {
            if (registry.containsKey(loadBalancer.getName())) {
                throw new IllegalArgumentException(String.format("LoadBalancer name: %s repeated", loadBalancer.getName()));
            }
            registry.put(loadBalancer.getName(), loadBalancer);
        });
    }

    public static LoadBalancer getLoadBalancer(String name) {
        return Optional.ofNullable(registry.get(name))
                       .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown LoadBalancer name: %s", name)));
    }
}
