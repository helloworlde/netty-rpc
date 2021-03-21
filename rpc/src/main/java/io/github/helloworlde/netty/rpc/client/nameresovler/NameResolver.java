package io.github.helloworlde.netty.rpc.client.nameresovler;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;

public abstract class NameResolver {

    protected String authority;

    protected LoadBalancer loadBalancer;

    public NameResolver(String authority, LoadBalancer loadBalancer) {
        this.authority = authority;
        this.loadBalancer = loadBalancer;
    }

    public abstract void resolve();
}
