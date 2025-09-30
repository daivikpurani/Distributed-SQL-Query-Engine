package com.distributed.sql.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition for the coordinator
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: query.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class CoordinatorServiceGrpc {

  private CoordinatorServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "distributed.sql.CoordinatorService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.QueryRequest,
      com.distributed.sql.common.proto.QueryProto.QueryResponse> getExecuteQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ExecuteQuery",
      requestType = com.distributed.sql.common.proto.QueryProto.QueryRequest.class,
      responseType = com.distributed.sql.common.proto.QueryProto.QueryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.QueryRequest,
      com.distributed.sql.common.proto.QueryProto.QueryResponse> getExecuteQueryMethod() {
    io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.QueryRequest, com.distributed.sql.common.proto.QueryProto.QueryResponse> getExecuteQueryMethod;
    if ((getExecuteQueryMethod = CoordinatorServiceGrpc.getExecuteQueryMethod) == null) {
      synchronized (CoordinatorServiceGrpc.class) {
        if ((getExecuteQueryMethod = CoordinatorServiceGrpc.getExecuteQueryMethod) == null) {
          CoordinatorServiceGrpc.getExecuteQueryMethod = getExecuteQueryMethod =
              io.grpc.MethodDescriptor.<com.distributed.sql.common.proto.QueryProto.QueryRequest, com.distributed.sql.common.proto.QueryProto.QueryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ExecuteQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.QueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.QueryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CoordinatorServiceMethodDescriptorSupplier("ExecuteQuery"))
              .build();
        }
      }
    }
    return getExecuteQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.StatusRequest,
      com.distributed.sql.common.proto.QueryProto.StatusResponse> getGetWorkerStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWorkerStatus",
      requestType = com.distributed.sql.common.proto.QueryProto.StatusRequest.class,
      responseType = com.distributed.sql.common.proto.QueryProto.StatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.StatusRequest,
      com.distributed.sql.common.proto.QueryProto.StatusResponse> getGetWorkerStatusMethod() {
    io.grpc.MethodDescriptor<com.distributed.sql.common.proto.QueryProto.StatusRequest, com.distributed.sql.common.proto.QueryProto.StatusResponse> getGetWorkerStatusMethod;
    if ((getGetWorkerStatusMethod = CoordinatorServiceGrpc.getGetWorkerStatusMethod) == null) {
      synchronized (CoordinatorServiceGrpc.class) {
        if ((getGetWorkerStatusMethod = CoordinatorServiceGrpc.getGetWorkerStatusMethod) == null) {
          CoordinatorServiceGrpc.getGetWorkerStatusMethod = getGetWorkerStatusMethod =
              io.grpc.MethodDescriptor.<com.distributed.sql.common.proto.QueryProto.StatusRequest, com.distributed.sql.common.proto.QueryProto.StatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWorkerStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.StatusRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.distributed.sql.common.proto.QueryProto.StatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CoordinatorServiceMethodDescriptorSupplier("GetWorkerStatus"))
              .build();
        }
      }
    }
    return getGetWorkerStatusMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CoordinatorServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceStub>() {
        @java.lang.Override
        public CoordinatorServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CoordinatorServiceStub(channel, callOptions);
        }
      };
    return CoordinatorServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CoordinatorServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceBlockingStub>() {
        @java.lang.Override
        public CoordinatorServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CoordinatorServiceBlockingStub(channel, callOptions);
        }
      };
    return CoordinatorServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CoordinatorServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CoordinatorServiceFutureStub>() {
        @java.lang.Override
        public CoordinatorServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CoordinatorServiceFutureStub(channel, callOptions);
        }
      };
    return CoordinatorServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition for the coordinator
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void executeQuery(com.distributed.sql.common.proto.QueryProto.QueryRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.QueryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExecuteQueryMethod(), responseObserver);
    }

    /**
     */
    default void getWorkerStatus(com.distributed.sql.common.proto.QueryProto.StatusRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.StatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWorkerStatusMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service CoordinatorService.
   * <pre>
   * Service definition for the coordinator
   * </pre>
   */
  public static abstract class CoordinatorServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return CoordinatorServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service CoordinatorService.
   * <pre>
   * Service definition for the coordinator
   * </pre>
   */
  public static final class CoordinatorServiceStub
      extends io.grpc.stub.AbstractAsyncStub<CoordinatorServiceStub> {
    private CoordinatorServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordinatorServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CoordinatorServiceStub(channel, callOptions);
    }

    /**
     */
    public void executeQuery(com.distributed.sql.common.proto.QueryProto.QueryRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.QueryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExecuteQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWorkerStatus(com.distributed.sql.common.proto.QueryProto.StatusRequest request,
        io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.StatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWorkerStatusMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service CoordinatorService.
   * <pre>
   * Service definition for the coordinator
   * </pre>
   */
  public static final class CoordinatorServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<CoordinatorServiceBlockingStub> {
    private CoordinatorServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordinatorServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CoordinatorServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.distributed.sql.common.proto.QueryProto.QueryResponse executeQuery(com.distributed.sql.common.proto.QueryProto.QueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExecuteQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.distributed.sql.common.proto.QueryProto.StatusResponse getWorkerStatus(com.distributed.sql.common.proto.QueryProto.StatusRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWorkerStatusMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service CoordinatorService.
   * <pre>
   * Service definition for the coordinator
   * </pre>
   */
  public static final class CoordinatorServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<CoordinatorServiceFutureStub> {
    private CoordinatorServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CoordinatorServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CoordinatorServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.distributed.sql.common.proto.QueryProto.QueryResponse> executeQuery(
        com.distributed.sql.common.proto.QueryProto.QueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExecuteQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.distributed.sql.common.proto.QueryProto.StatusResponse> getWorkerStatus(
        com.distributed.sql.common.proto.QueryProto.StatusRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWorkerStatusMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EXECUTE_QUERY = 0;
  private static final int METHODID_GET_WORKER_STATUS = 1;

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
        case METHODID_EXECUTE_QUERY:
          serviceImpl.executeQuery((com.distributed.sql.common.proto.QueryProto.QueryRequest) request,
              (io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.QueryResponse>) responseObserver);
          break;
        case METHODID_GET_WORKER_STATUS:
          serviceImpl.getWorkerStatus((com.distributed.sql.common.proto.QueryProto.StatusRequest) request,
              (io.grpc.stub.StreamObserver<com.distributed.sql.common.proto.QueryProto.StatusResponse>) responseObserver);
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
          getExecuteQueryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.distributed.sql.common.proto.QueryProto.QueryRequest,
              com.distributed.sql.common.proto.QueryProto.QueryResponse>(
                service, METHODID_EXECUTE_QUERY)))
        .addMethod(
          getGetWorkerStatusMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.distributed.sql.common.proto.QueryProto.StatusRequest,
              com.distributed.sql.common.proto.QueryProto.StatusResponse>(
                service, METHODID_GET_WORKER_STATUS)))
        .build();
  }

  private static abstract class CoordinatorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CoordinatorServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.distributed.sql.common.proto.QueryProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CoordinatorService");
    }
  }

  private static final class CoordinatorServiceFileDescriptorSupplier
      extends CoordinatorServiceBaseDescriptorSupplier {
    CoordinatorServiceFileDescriptorSupplier() {}
  }

  private static final class CoordinatorServiceMethodDescriptorSupplier
      extends CoordinatorServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    CoordinatorServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (CoordinatorServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CoordinatorServiceFileDescriptorSupplier())
              .addMethod(getExecuteQueryMethod())
              .addMethod(getGetWorkerStatusMethod())
              .build();
        }
      }
    }
    return result;
  }
}
