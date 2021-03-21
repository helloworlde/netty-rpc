package io.github.helloworlde.netty.rpc.client.lb;

import io.github.helloworlde.netty.rpc.client.transport.Transport;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class LoadBalancer {

    List<Transport> transports = new CopyOnWriteArrayList<>();

    public abstract Transport choose();

    public void updateAddress(SocketAddress address) throws Exception {
        synchronized (LoadBalancer.class) {
            boolean present = transports.stream()
                                        .anyMatch(t -> t.getAddress().equals(address));
            if (!present) {
                Transport transport = new Transport(address);
                transport.init();
                transport.doConnect();
                this.transports.clear();
                this.transports.add(transport);
            }
        }

    }

    public void updateAddress(List<SocketAddress> addresses) {
    }

}
