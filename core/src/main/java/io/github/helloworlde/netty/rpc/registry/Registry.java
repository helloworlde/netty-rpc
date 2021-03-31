package io.github.helloworlde.netty.rpc.registry;

import java.util.Map;

public abstract class Registry {

    public abstract boolean register(String name, String address, int port, Map<String, String> metadata);

    public abstract boolean unregister();
}
