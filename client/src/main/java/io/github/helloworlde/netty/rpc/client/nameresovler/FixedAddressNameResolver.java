package io.github.helloworlde.netty.rpc.client.nameresovler;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;

public class FixedAddressNameResolver extends NameResolver {

    private List<SocketAddress> addresses;

    public FixedAddressNameResolver(SocketAddress... addresses) {
        this.addresses = Arrays.asList(addresses);
    }

    @Override
    public void refresh() {
        loadBalancer.onResult(addresses);
    }
}
