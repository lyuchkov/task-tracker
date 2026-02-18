package tracker.http.hadlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.managers.TaskManager;
import tracker.tasks.Task;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, "Method Not Allowed", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) {
        Optional<Long> idOpt = getTaskIdFromPath(exchange);

        if (idOpt.isPresent()) {
            Task task = manager.getTask(idOpt.get());
            if (task != null) {
                sendResponse(exchange, gson.toJson(task), 200);
            } else {
                sendNotFoundResponse(exchange, "Task with id " + idOpt.get() + " not found");
            }
        } else {
            sendResponse(exchange, gson.toJson(manager.getAllTasks()), 200);
        }
    }

    private void handlePost(HttpExchange exchange) {
        String body = readBody(exchange);
        Task task = gson.fromJson(body, Task.class);

        try {
            if (task.getId() != 0) {
                manager.updateTask(task);
                sendResponse(exchange, "Task updated", 201);
            } else {
                manager.createTask(task);
                sendResponse(exchange, "Task created", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, "Task overlaps with existing tasks");
        }
    }

    private void handleDelete(HttpExchange exchange) {
        Optional<Long> idOpt = getTaskIdFromPath(exchange);
        if (idOpt.isPresent()) {
            manager.deleteTask(idOpt.get());
            sendResponse(exchange, "Task deleted", 200);
        } else {
            sendResponse(exchange, "ID is required", 400);
        }
    }
}