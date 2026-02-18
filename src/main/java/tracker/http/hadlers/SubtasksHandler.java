package tracker.http.hadlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.managers.TaskManager;
import tracker.tasks.Subtask;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubtasksHandler(TaskManager manager) {
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
            Subtask subtask = manager.getSubtask(idOpt.get());
            if (subtask != null) {
                sendResponse(exchange, gson.toJson(subtask), 200);
            } else {
                sendNotFoundResponse(exchange, "Subtask not found");
            }
        } else {
            sendResponse(exchange, gson.toJson(manager.getAllSubtasks()), 200);
        }
    }

    private void handlePost(HttpExchange exchange) {
        String body = readBody(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);
        try {
            if (subtask.getId() != 0) {
                manager.updateSubtask(subtask);
                sendResponse(exchange, "Subtask updated", 201);
            } else {
                manager.createSubtask(subtask);
                sendResponse(exchange, "Subtask created", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, "Subtask overlaps");
        }
    }

    private void handleDelete(HttpExchange exchange) {
        Optional<Long> idOpt = getTaskIdFromPath(exchange);
        if (idOpt.isPresent()) {
            manager.deleteSubtask(idOpt.get());
            sendResponse(exchange, "Subtask deleted", 200);
        } else {
            sendResponse(exchange, "ID is required", 400);
        }
    }
}