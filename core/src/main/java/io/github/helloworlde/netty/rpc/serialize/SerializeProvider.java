package io.github.helloworlde.netty.rpc.serialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class SerializeProvider {
    private static final Logger log = LoggerFactory.getLogger(SerializeProvider.class);

    private static final Map<Integer, Serialize> idRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Serialize> nameRegistry = new ConcurrentHashMap<>();

    static {
        ServiceLoader<Serialize> serializes = ServiceLoader.load(Serialize.class);
        serializes.forEach(e -> {
            log.info("加载 Serialize 策略, id: {}, name: {}", e.getId(), e.getName());
            if (idRegistry.containsKey(e.getId())) {
                throw new IllegalArgumentException(String.format("Serialize id %d repeated for class: %s", e.getId(), e.getClass().getName()));
            }
            idRegistry.put(e.getId(), e);
            if (nameRegistry.containsKey(e.getName())) {
                throw new IllegalArgumentException(String.format("Serialize name %s repeated for class: %s", e.getName(), e.getClass().getName()));
            }
            nameRegistry.put(e.getName(), e);
        });
    }

    public static Serialize getSerialize(Integer id) {
        return Optional.ofNullable(idRegistry.get(id))
                       .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown Serialize id: %d", id)));
    }

    public static Serialize getSerializeByName(String name) {
        return Optional.ofNullable(nameRegistry.get(name))
                       .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown Serialize name: %s", name)));
    }
}
