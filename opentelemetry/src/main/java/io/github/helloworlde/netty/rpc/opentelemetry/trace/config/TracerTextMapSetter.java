package io.github.helloworlde.netty.rpc.opentelemetry.trace.config;

import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class TracerTextMapSetter implements TextMapSetter<Map<String, Object>> {

    @Override
    public void set(@Nullable Map<String, Object> carrier, String key, String value) {
        if (Objects.isNull(carrier)) {
            log.debug("carrier 是 null");
            return;
        }
        log.debug("向 Trace 添加 key: {}, value: {}", key, value);
        carrier.put(key, value);
    }
}
