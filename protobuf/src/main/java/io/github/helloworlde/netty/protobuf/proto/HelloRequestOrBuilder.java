// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: protobuf/src/main/resources/hello-service.proto

package io.github.helloworlde.netty.protobuf.proto;

public interface HelloRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:io.github.helloworlde.netty.protobuf.HelloRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 id = 1;</code>
   * @return The id.
   */
  long getId();

  /**
   * <code>string message = 2;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>string message = 2;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>int64 timestamp = 3;</code>
   * @return The timestamp.
   */
  long getTimestamp();
}
