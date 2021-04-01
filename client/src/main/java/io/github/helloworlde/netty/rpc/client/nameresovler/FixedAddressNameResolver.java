package io.github.helloworlde.netty.rpc.client.nameresovler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FixedAddressNameResolver extends NameResolver {

    private List<SocketAddress> addresses;

    public FixedAddressNameResolver(List<ServerAddress> addresses) {
        if (addresses == null) {
            this.addresses = Collections.emptyList();
        } else {
            this.addresses = addresses.stream()
                                      .map(address -> new InetSocketAddress(address.getHostname(), address.getPort()))
                                      .collect(Collectors.toList());
        }
    }

    public FixedAddressNameResolver(SocketAddress... addresses) {
        this.addresses = Arrays.asList(addresses);
    }

    @Override
    public void refresh() {
        loadBalancer.onResult(addresses);
    }
}
