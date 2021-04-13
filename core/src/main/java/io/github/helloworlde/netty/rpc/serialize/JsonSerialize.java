package io.github.helloworlde.netty.rpc.serialize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerialize implements Serialize {

    private static JsonSerialize jsonSerialize;

    private final ObjectMapper objectMapper;

    public JsonSerialize() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public byte[] serialize(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(object);
    }

    @Override
    public Integer getId() {
        return 1;
    }

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> className) throws Exception {
        return objectMapper.readValue(bytes, className);
    }
}
