package io.github.helloworlde.netty.rpc.registry;

public abstract class Registry {

    public abstract boolean register(ServiceInfo serviceInfo);

    public abstract boolean deRegister(ServiceInfo serviceInfo);
}
