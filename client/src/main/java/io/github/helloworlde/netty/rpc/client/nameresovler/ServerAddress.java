package io.github.helloworlde.netty.rpc.client.nameresovler;

import lombok.Data;

@Data
public class ServerAddress {

    private String hostname;

    private Integer port;
}
