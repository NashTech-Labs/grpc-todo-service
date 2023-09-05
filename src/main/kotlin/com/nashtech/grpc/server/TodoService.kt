package com.nashtech.grpc.server

import com.nashtech.protoBuf.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

/**
 * gRPC service implementation for managing TODO items.
 */
@GrpcService
class TodoService : TodoServiceGrpc.TodoServiceImplBase() {
    private val todos = mutableMapOf<Int, Todo>()

    /**
     * Creates a new TODO item.
     *
     * @param request          The request containing TODO data.
     * @param responseObserver The observer to send the response.
     */
    override fun createTodo(
        request: CreateTodoRequest?,
        responseObserver: StreamObserver<Todo>?
    ) {
        if (request != null) {
            val id = todos.size + 1
            val newTodo = Todo.newBuilder()
                .setId(id)
                .setTitle(request.title)
                .setDescription(request.description)
                .setDone(false)
                .build()

            todos[id] = newTodo
            responseObserver?.onNext(newTodo)
            responseObserver?.onCompleted()
        } else {
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid request: Todo data is missing.")
                .asRuntimeException())
        }
    }

    /**
     * Retrieves a TODO item by its ID.
     *
     * @param request          The request containing the ID of the TODO item to retrieve.
     * @param responseObserver The observer to send the response.
     */
    override fun getTodo(
        request: TodoId?,
        responseObserver: StreamObserver<Todo>?
    ) {
        if (request != null && todos.containsKey(request.id)) {
            val todo = todos[request.id]
            responseObserver?.onNext(todo)
            responseObserver?.onCompleted()
        } else {
            responseObserver?.onError(Status.NOT_FOUND
                .withDescription("Todo with ID ${request?.id} not found.")
                .asRuntimeException())
        }
    }

    /**
     * Updates an existing TODO item.
     *
     * @param request          The request containing the updated TODO data.
     * @param responseObserver The observer to send the response.
     */
    override fun updateTodo(
        request: UpdateTodoRequest?,
        responseObserver: StreamObserver<UpdateTodoResponse>?
    ) {
        val responseBuilder = UpdateTodoResponse.newBuilder()

        if (request != null && todos.containsKey(request.id)) {
            val updatedTodo = todos[request.id]!!.toBuilder()
                .setTitle(request.title)
                .setDescription(request.description)
                .setDone(request.done)
                .build()

            todos[request.id] = updatedTodo

            responseBuilder.message = "Todo with title '${request.title}' updated successfully."
            responseBuilder.updatedTodo = updatedTodo
            responseObserver?.onNext(responseBuilder.build())
            responseObserver?.onCompleted()
        } else {
            responseBuilder.message = "Todo with ID ${request?.id} not found."
            responseObserver?.onNext(responseBuilder.build())
            responseObserver?.onCompleted()
        }
    }

    /**
     * Deletes a TODO item by its ID.
     *
     * @param request          The request containing the ID of the TODO item to delete.
     * @param responseObserver The observer to send the response.
     */
    override fun deleteTodo(
        request: TodoId?,
        responseObserver: StreamObserver<DeleteTodoResponse>?
    ) {
        val responseBuilder = DeleteTodoResponse.newBuilder()

        if (request != null && todos.containsKey(request.id)) {
            val removedTodo = todos.remove(request.id)
            responseBuilder.status = DeleteTodoResponse.Status.DELETED_SUCCESSFULLY
            responseBuilder.message = "Todo with ID ${request.id} deleted successfully."
            responseObserver?.onNext(responseBuilder.build())
            responseObserver?.onCompleted()
        } else {
            responseBuilder.status = DeleteTodoResponse.Status.ID_NOT_FOUND
            responseBuilder.message = "Todo with ID ${request?.id} not found."
            responseObserver?.onNext(responseBuilder.build())
            responseObserver?.onCompleted()
        }
    }

    /**
     * Retrieves a list of all TODO items.
     *
     * @param request          The request.
     * @param responseObserver The observer to send the response.
     */
    override fun listAllTodo(
        request: TodoAll?,
        responseObserver: StreamObserver<ListTodoResponse>?
    ) {
        val todoList = todos.values.toList()

        val listResponse = ListTodoResponse.newBuilder()
            .addAllTodo(todoList)
            .build()

        responseObserver?.onNext(listResponse)
        responseObserver?.onCompleted()
    }
}
