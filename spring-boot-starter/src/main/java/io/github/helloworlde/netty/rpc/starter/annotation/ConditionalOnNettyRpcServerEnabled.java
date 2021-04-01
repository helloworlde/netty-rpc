package io.github.helloworlde.netty.rpc.starter.annotation;


import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "netty.rpc.server.enabled", matchIfMissing = true)
@ConditionalOnBean(annotation = NettyRpcService.class, value = Object.class)
public @interface ConditionalOnNettyRpcServerEnabled {
}
