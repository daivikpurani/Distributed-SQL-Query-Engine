package com.distributed.sql.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition for worker nodes
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: query.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class WorkerServiceGrpc {

  private WorkerServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "distributed.sql.WorkerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.TaskRequest,
      com.distributed.sql.common.proto.QueryProto.TaskResponse> getExecuteTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExecuteTask",
      requestType = com.distributed.sql.common.proto.QueryProto.TaskRequest.class,
      responseType = com.distributed.sql.common.proto.QueryProto.TaskResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.TaskRequest,
      com.distributed.sql.common.proto.QueryProto.TaskResponse> getExecuteTaskMethod() {
    io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.TaskRequest, com.distributed.sql.common.proto.QueryProto.TaskResponse> getExecuteTaskMethod;
    if ((getExecuteTaskMethod = WorkerServiceGrpc.getExecuteTaskMethod) == null) {
      synchronized (WorkerServiceGrpc.class) {
        if ((getExecuteTaskMethod = WorkerServiceGrpc.getExecuteTaskMethod) == null) {
          WorkerServiceGrpc.getExecuteTaskMethod = getExecuteTaskMethod =
              io.grpc.MethodDescriptor.<com.distributed.sql.common.proto.QueryProto.TaskRequest, com.distributed.sql.common.proto.QueryProto.TaskResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExecuteTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.TaskRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.TaskResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WorkerServiceMethodDescriptorSupplier("ExecuteTask"))
              .build();
        }
      }
    }
    return getExecuteTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.CheckpointRequest,
      com.distributed.sql.common.proto.QueryProto.CheckpointResponse> getCheckpointMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Checkpoint",
      requestType = com.distributed.sql.common.proto.QueryProto.CheckpointRequest.class,
      responseType = com.distributed.sql.common.proto.QueryProto.CheckpointResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.CheckpointRequest,
      com.distributed.sql.common.proto.QueryProto.CheckpointResponse> getCheckpointMethod() {
    io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.CheckpointRequest, com.distributed.sql.common.proto.QueryProto.CheckpointResponse> getCheckpointMethod;
    if ((getCheckpointMethod = WorkerServiceGrpc.getCheckpointMethod) == null) {
      synchronized (WorkerServiceGrpc.class) {
        if ((getCheckpointMethod = WorkerServiceGrpc.getCheckpointMethod) == null) {
          WorkerServiceGrpc.getCheckpointMethod = getCheckpointMethod =
              io.grpc.MethodDescriptor.<com.distributed.sql.common.proto.QueryProto.CheckpointRequest, com.distributed.sql.common.proto.QueryProto.CheckpointResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Checkpoint"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.CheckpointRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.CheckpointResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WorkerServiceMethodDescriptorSupplier("Checkpoint"))
              .build();
        }
      }
    }
    return getCheckpointMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.HealthRequest,
      com.distributed.sql.common.proto.QueryProto.HealthResponse> getHealthCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HealthCheck",
      requestType = com.distributed.sql.common.proto.QueryProto.HealthRequest.class,
      responseType = com.distributed.sql.common.proto.QueryProto.HealthResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.HealthRequest,
      com.distributed.sql.common.proto.QueryProto.HealthResponse> getHealthCheckMethod() {
    io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.HealthRequest, com.distributed.sql.common.proto.QueryProto.HealthResponse> getHealthCheckMethod;
    if ((getHealthCheckMethod = WorkerServiceGrpc.getHealthCheckMethod) == null) {
      synchronized (WorkerServiceGrpc.class) {
        if ((getHealthCheckMethod = WorkerServiceGrpc.getHealthCheckMethod) == null) {
          WorkerServiceGrpc.getHealthCheckMethod = getHealthCheckMethod =
              io.grpc.MethodDescriptor.<com.distributed.sql.common.proto.QueryProto.HealthRequest, com.distributed.sql.common.proto.QueryProto.HealthResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HealthCheck"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.HealthRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.HealthResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WorkerServiceMethodDescriptorSupplier("HealthCheck"))
              .build();
        }
      }
    }
    return getHealthCheckMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WorkerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WorkerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WorkerServiceStub>() {
        @java.lang.Override
        public WorkerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WorkerServiceStub(channel, callOptions);
        }
      };
    return WorkerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WorkerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WorkerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WorkerServiceBlockingStub>() {
        @java.lang.Override
        public WorkerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WorkerServiceBlockingStub(channel, callOptions);
        }
      };
    return WorkerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WorkerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WorkerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WorkerServiceFutureStub>() {
        @java.lang.Override
        public WorkerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WorkerServiceFutureStub(channel, callOptions);
        }
      };
    return WorkerServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition for worker nodes
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void executeTask(com.distributed.sql.common.proto.QueryProto.TaskRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.TaskResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteTaskMethod(), responseObserver);
    }

    /**
     */
    default void checkpoint(com.distributed.sql.common.proto.QueryProto.CheckpointRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.CheckpointResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCheckpointMethod(), responseObserver);
    }

    /**
     */
    default void healthCheck(com.distributed.sql.common.proto.QueryProto.HealthRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.HealthResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHealthCheckMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service WorkerService.
   * <pre>
   * Service definition for worker nodes
   * </pre>
   */
  public static abstract class WorkerServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return WorkerServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service WorkerService.
   * <pre>
   * Service definition for worker nodes
   * </pre>
   */
  public static final class WorkerServiceStub
      extends io.grpc.stub.AbstractAsyncStub<WorkerServiceStub> {
    private WorkerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WorkerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WorkerServiceStub(channel, callOptions);
    }

    /**
     */
    public void executeTask(com.distributed.sql.common.proto.QueryProto.TaskRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.TaskResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExecuteTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkpoint(com.distributed.sql.common.proto.QueryProto.CheckpointRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.CheckpointResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCheckpointMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void healthCheck(com.distributed.sql.common.proto.QueryProto.HealthRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.HealthResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service WorkerService.
   * <pre>
   * Service definition for worker nodes
   * </pre>
   */
  public static final class WorkerServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<WorkerServiceBlockingStub> {
    private WorkerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WorkerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WorkerServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.distributed.sql.common.proto.QueryProto.TaskResponse executeTask(com.distributed.sql.common.proto.QueryProto.TaskRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExecuteTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.distributed.sql.common.proto.QueryProto.CheckpointResponse checkpoint(com.distributed.sql.common.proto.QueryProto.CheckpointRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCheckpointMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.distributed.sql.common.proto.QueryProto.HealthResponse healthCheck(com.distributed.sql.common.proto.QueryProto.HealthRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHealthCheckMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service WorkerService.
   * <pre>
   * Service definition for worker nodes
   * </pre>
   */
  public static final class WorkerServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<WorkerServiceFutureStub> {
    private WorkerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WorkerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WorkerServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.distributed.sql.common.proto.QueryProto.TaskResponse> executeTask(
        com.distributed.sql.common.proto.QueryProto.TaskRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExecuteTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.distributed.sql.common.proto.QueryProto.CheckpointResponse> checkpoint(
        com.distributed.sql.common.proto.QueryProto.CheckpointRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCheckpointMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.distributed.sql.common.proto.QueryProto.HealthResponse> healthCheck(
        com.distributed.sql.common.proto.QueryProto.HealthRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EXECUTE_TASK = 0;
  private static final int METHODID_CHECKPOINT = 1;
  private static final int METHODID_HEALTH_CHECK = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EXECUTE_TASK:
          serviceImpl.executeTask((com.distributed.sql.common.proto.QueryProto.TaskRequest) request,
              (io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.TaskResponse>) responseObserver);
          break;
        case METHODID_CHECKPOINT:
          serviceImpl.checkpoint((com.distributed.sql.common.proto.QueryProto.CheckpointRequest) request,
              (io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.CheckpointResponse>) responseObserver);
          break;
        case METHODID_HEALTH_CHECK:
          serviceImpl.healthCheck((com.distributed.sql.common.proto.QueryProto.HealthRequest) request,
              (io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.HealthResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getExecuteTaskMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.distributed.sql.common.proto.QueryProto.TaskRequest,
              com.distributed.sql.common.proto.QueryProto.TaskResponse>(
                service, METHODID_EXECUTE_TASK)))
        .addMethod(
          getCheckpointMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.distributed.sql.common.proto.QueryProto.CheckpointRequest,
              com.distributed.sql.common.proto.QueryProto.CheckpointResponse>(
                service, METHODID_CHECKPOINT)))
        .addMethod(
          getHealthCheckMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.distributed.sql.common.proto.QueryProto.HealthRequest,
              com.distributed.sql.common.proto.QueryProto.HealthResponse>(
                service, METHODID_HEALTH_CHECK)))
        .build();
  }

  private static abstract class WorkerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WorkerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.distributed.sql.common.proto.QueryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("WorkerService");
    }
  }

  private static final class WorkerServiceFileDescriptorSupplier
      extends WorkerServiceBaseDescriptorSupplier {
    WorkerServiceFileDescriptorSupplier() {}
  }

  private static final class WorkerServiceMethodDescriptorSupplier
      extends WorkerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    WorkerServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WorkerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WorkerServiceFileDescriptorSupplier())
              .addMethod(getExecuteTaskMethod())
              .addMethod(getCheckpointMethod())
              .addMethod(getHealthCheckMethod())
              .build();
        }
      }
    }
    return result;
  }
}
