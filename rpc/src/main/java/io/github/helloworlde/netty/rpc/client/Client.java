package io.github.helloworlde.netty.rpc.client;

import io.github.helloworlde.netty.rpc.client.transport.Transport;
import io.github.helloworlde.netty.rpc.error.RpcException;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class Client {

    private String host;

    private Integer port;

    private Transport transport;

    private final AtomicLong requestSeq = new AtomicLong();

    private ConcurrentHashMap<Long, ResponseFuture<Object>> paddingRequests = new ConcurrentHashMap<>();

    public Client forAddress(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public Client start() throws Exception {
        this.transport = new Transport(this);
        transport.doOpen();
        transport.doConnect(host, port);
        return this;
    }

    public void shutdown() {
        try {
            log.info("Shutting down...");
            transport.shutdown();
        } catch (Exception e) {
            log.error("关闭错误: {}", e.getMessage(), e);
        }
    }

    private Request createRequest(Class<?> proxyClass, String methodName, Object[] params) throws Exception {
        return Request.builder()
                      .requestId(requestSeq.getAndIncrement())
                      .serviceName(proxyClass.getName())
                      .methodName(methodName)
                      .params(params)
                      .build();
    }

    public Object sendRequest(Class<?> serviceClass, String methodName, Object[] args) throws Exception {
        while (!this.transport.isActive()) {
            log.debug("Channel is not active, waiting...");
        }

        Request request = createRequest(serviceClass, methodName, args);
        ResponseFuture<Object> responseFuture = new ResponseFuture<>();
        this.paddingRequests.putIfAbsent(request.getRequestId(), responseFuture);

        transport.write(request);
        return waitResponse(responseFuture);
    }

    private Object waitResponse(ResponseFuture<Object> future) throws Exception {
        return future.get();
    }

    public void receiveResponse(Response msg) {
        if (msg.getError() == null) {
            paddingRequests.get(msg.getRequestId())
                           .setSuccess(msg.getBody());
        } else {
            receiveError(msg.getRequestId(), new RpcException("Response failed: " + msg.getError()));
        }
    }

    public void receiveError(Long requestId, Throwable cause) {
        paddingRequests.get(requestId)
                       .setFailure(cause);
    }

    public void sendComplete(Long requestId) {
        log.trace("Send request: {} success", requestId);
    }

    public void completeRequest(Long requestId) {
        log.trace("Request: {} Completed", requestId);
        this.paddingRequests.remove(requestId);
    }
}
