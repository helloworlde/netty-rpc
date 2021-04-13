package io.github.helloworlde.netty.rpc.serialize;

public interface Serialize {

    Integer getId();

    String getName();

    <T> byte[] serialize(T object) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> className) throws Exception;
}
