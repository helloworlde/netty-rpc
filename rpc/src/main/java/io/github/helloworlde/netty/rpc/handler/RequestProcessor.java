package io.github.helloworlde.netty.rpc.handler;

import io.github.helloworlde.netty.rpc.model.Header;
import io.github.helloworlde.netty.rpc.model.Request;
import io.github.helloworlde.netty.rpc.model.Response;
import io.github.helloworlde.netty.rpc.model.ServiceDetail;
import io.github.helloworlde.netty.rpc.model.Status;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RequestProcessor extends SimpleChannelInboundHandler<Request> {

    private Map<String, ServiceDetail<?>> serviceDetailMap;

    private ConcurrentHashMap<Long, Request> pendingRequest = new ConcurrentHashMap<>();

    public RequestProcessor(Map<String, ServiceDetail<?>> serviceDetailMap) {
        this.serviceDetailMap = serviceDetailMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        Long requestId = request.getRequestId();
        pendingRequest.put(requestId, request);

        Header header = request.getHeader();
        String serviceName = header.getClassName();
        String methodName = header.getMethodName();

        ServiceDetail<?> serviceDetail = serviceDetailMap.get(serviceName);
        if (Objects.isNull(serviceDetail)) {
            log.info("Service Not Found");
            Response response = Response.builder()
                                        .requestId(requestId)
                                        .status(Status.EXCEPTION)
                                        .message("Service Not Found")
                                        .build();
            ctx.write(response);
            return;
        }
        Method method = serviceDetail.getMethods().get(methodName);
        if (Objects.isNull(method)) {
            log.info("Method Not Found");
            Response response = Response.builder()
                                        .requestId(requestId)
                                        .status(Status.EXCEPTION)
                                        .message("Method Not Found")
                                        .build();
            ctx.write(response);
            return;
        }

        Object body = request.getBody();
        Object responseBody = method.invoke(serviceDetail.getInstance(), body);

        Response response = Response.builder()
                                    .requestId(requestId)
                                    .status(Status.SUCCESS)
                                    .body(responseBody)
                                    .build();
        ctx.write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("Read Complete");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("异常:{}", cause.getMessage(), cause);
        ctx.close();
    }
}
