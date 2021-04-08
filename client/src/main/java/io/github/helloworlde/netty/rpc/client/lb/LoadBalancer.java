package io.github.helloworlde.netty.rpc.client.lb;

import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.client.transport.TransportFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public abstract class LoadBalancer {

    private TransportFactory transportFactory;

    protected transient List<Transport> transports = new CopyOnWriteArrayList<>();

    public Transport chooseTransport() throws Exception {
        Transport transport = choose();
        int retryTime = 1;
        while (!transport.isActive()) {
            log.warn("Channel {} is not active, waiting...", transport);
            if (retryTime >= 5) {
                throw new IllegalStateException("选择的服务实例都不可用");
            }
            try {
                transport.doConnect();
            } catch (Exception e) {
                log.error("连接: {} 错误: {}", transport, e.getMessage());
            }
            // 重新选择节点
            if (!transport.isActive()) {
                log.warn("Reconnect {} failed, choose others transport", transport);
                transport = choose();
            }
            retryTime++;
        }
        return transport;
    }

    public abstract Transport choose();

    public void setTransportFactory(TransportFactory transportFactory) {
        this.transportFactory = transportFactory;
    }

    public synchronized void onResult(List<SocketAddress> resolvedAddresses) {
        List<Transport> newTransport = new ArrayList<>(this.transports);
        List<SocketAddress> addresses = new ArrayList<>(resolvedAddresses);

        log.debug("更新实例地址: {}", addresses);
        // 当前的地址
        List<SocketAddress> presentAddresses = newTransport.stream()
                                                           .map(Transport::getAddress)
                                                           .collect(Collectors.toList());

        // 过滤需要删除的地址
        List<SocketAddress> needRemovedAddresses = presentAddresses.stream()
                                                                   .filter(address -> !addresses.contains(address))
                                                                   .collect(Collectors.toList());

        log.debug("需要删除的地址: {}", needRemovedAddresses);
        addresses.removeAll(presentAddresses);
        log.debug("删除后的地址: {}", addresses);

        // 为新的地址创建连接
        addresses.stream()
                 .map(address -> transportFactory.createTransport(address))
                 .forEach(transport -> {
                     try {
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
        log.debug("更新后的列表: {}", this.transports.stream().map(Transport::getAddress).collect(Collectors.toList()));
    }

    public void shutdown() {
        this.transports.forEach(Transport::shutdown);
    }
}
