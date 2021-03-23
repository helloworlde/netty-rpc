package io.github.helloworlde.netty.rpc.client.lb;

import io.github.helloworlde.netty.rpc.client.transport.Transport;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public abstract class LoadBalancer {

    protected transient List<Transport> transports = new CopyOnWriteArrayList<>();

    public abstract Transport choose();

    public synchronized void updateAddress(List<SocketAddress> resolvedAddresses) {
        List<Transport> newTransport = new ArrayList<>(this.transports);
        List<SocketAddress> addresses = new ArrayList<>(resolvedAddresses);

        log.info("更新实例地址: {}", addresses);
        // 当前的地址
        List<SocketAddress> presentAddresses = newTransport.stream()
                                                           .map(Transport::getAddress)
                                                           .collect(Collectors.toList());

        // 过滤需要删除的地址
        List<SocketAddress> needRemovedAddresses = presentAddresses.stream()
                                                                   .filter(address -> !addresses.contains(address))
                                                                   .collect(Collectors.toList());

        log.info("需要删除的地址: {}", needRemovedAddresses);
        addresses.removeAll(presentAddresses);
        log.info("删除后的地址: {}", addresses);

        // 为新的地址创建连接
        addresses.stream()
                 .map(Transport::new)
                 .forEach(transport -> {
                     try {
                         transport.init();
                         transport.doConnect();
                         newTransport.add(transport);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 });

        // 删除要移除的地址
        transports.stream()
                  .filter(transport -> needRemovedAddresses.contains(transport.getAddress()))
                  .forEach(transport -> {
                      transport.shutdown();
                      newTransport.remove(transport);
                  });

        this.transports = newTransport;
        log.info("更新后的列表: {}", this.transports.stream().map(Transport::getAddress).collect(Collectors.toList()));
    }
}
