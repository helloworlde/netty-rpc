package io.github.helloworlde.netty.rpc.registry;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class NoopRegistry extends Registry {

    @Override
    public boolean unregister() {
        return true;
    }

    @Override
    public boolean register(String name, String address, int port, Map<String, String> metadata) {
        return true;
    }
}
