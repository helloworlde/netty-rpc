package io.github.helloworlde.netty.rpc.serialize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public class JsonSerialize implements Serialize {

    private static JsonSerialize jsonSerialize;

    private final ObjectMapper objectMapper;

    private JsonSerialize() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public static JsonSerialize getInstance() {
        if (Objects.isNull(jsonSerialize)) {
            synchronized (JsonSerialize.class) {
                if (Objects.isNull(jsonSerialize)) {
                    jsonSerialize = new JsonSerialize();
                }
            }
        }
        return jsonSerialize;
    }

    @Override
    public byte[] serialize(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> className) throws Exception {
        return objectMapper.readValue(bytes, className);
    }
}
