package io.github.helloworlde.netty.rpc.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Context {

    private static final ThreadLocal<Context> storage = new ThreadLocal<>();

    private static final Map<String, Object> attribute = new ConcurrentHashMap<>();

    private Context() {
    }

    public static Context current() {
        Context current = Optional.ofNullable(storage.get())
                                  .orElse(new Context());
        storage.set(current);
        return current;
    }

    public Context withTimeout(Long timeout) {
        attribute.put("TIMEOUT", timeout);
        return this;
    }

    public Long getTimeout() {
        return (Long) attribute.get("TIMEOUT");
    }

    public Context withAttribute(String name, Object value) {
        attribute.put(name, value);
        return this;
    }

    public Object getAttribute(String name) {
        return attribute.get(name);
    }

    public void clear() {
        attribute.clear();
    }
}
