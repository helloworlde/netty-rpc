package io.github.helloworlde.netty.rpc.opentelemetry.trace.config;

import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class TracerTextMapGetter implements TextMapGetter<Map<String, Object>> {

    @Override
    public Iterable<String> keys(Map<String, Object> carrier) {
        return carrier.keySet();
    }

    @Nullable
    @Override
    public String get(@Nullable Map<String, Object> carrier, String key) {
        if (Objects.nonNull(carrier) && carrier.containsKey(key)) {
            return carrier.get(key).toString();
        }
        return null;
    }
}
