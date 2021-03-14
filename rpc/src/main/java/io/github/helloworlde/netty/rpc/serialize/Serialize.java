package io.github.helloworlde.netty.rpc.serialize;

public interface Serialize {

    byte[] serialize(Object object) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> className) throws Exception;
}
