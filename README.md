# Netty RPC

> 基于 Netty 手动实现一个 Java RPC 框架

## 功能

- Java 服务
- SpringBoot Starter
- 服务注册和服务发现
- 可扩展负载均衡、服务注册、服务发现

## 快速使用

- [Java](./doc/Java.md)
- [SpringBoot](./doc/SpringBoot.md)
- [服务发现](./doc/NameResolver.md)
- [注册中心](./doc/Registry.md)
- [负载均衡](./doc/LoadBalancer.md)


## 协议

使用自定义协议，格式为：`MagicNumber + Serialize + Length + Body`

- `MagicNumber` 为 `0x1024`，是一个 int 值，长度为 4 个字节
- `Serialize` 表示序列化方式，默认使用 JSON 协议，是一个 int 值，长度为 4 个字节
- `Length` 表示 `Body` 的长度，是一个 int 值，长度为 4 个字节
- `Body` 表示请求的具体内容，长度为 `Length` 个字节
