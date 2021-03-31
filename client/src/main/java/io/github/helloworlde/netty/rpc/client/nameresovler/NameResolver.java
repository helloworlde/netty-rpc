package io.github.helloworlde.netty.rpc.client.nameresovler;

import io.github.helloworlde.netty.rpc.client.lb.LoadBalancer;
import lombok.Data;

@Data
public abstract class NameResolver {

    protected String authority;

    protected LoadBalancer loadBalancer;

    public void start() {
        this.refresh();
    }

    public void shutdown() {

    }

    public abstract void refresh();
}
