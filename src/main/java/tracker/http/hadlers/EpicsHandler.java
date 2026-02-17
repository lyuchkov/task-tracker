package tracker.http.hadlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicsHandler(TaskManager manager) {
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
                    sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length == 4 && parts[3].equals("subtasks")) {
            handleGetEpicSubtasks(exchange, Long.parseLong(parts[2]));
            return;
        }

        Optional<Long> idOpt = getTaskIdFromPath(exchange);
        if (idOpt.isPresent()) {
            Epic epic = manager.getEpic(idOpt.get());
            if (epic != null) {
                sendResponse(exchange, gson.toJson(epic), 200);
            } else {
                sendEpicNotFoundResponse(exchange);
            }
        } else {
            sendResponse(exchange, gson.toJson(manager.getAllEpics()), 200);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, long epicId) {
        Epic epic = manager.getEpic(epicId);
        if (epic != null) {
            sendResponse(exchange, gson.toJson(manager.getEpicSubtasks(epicId)), 200);
        } else {
            sendEpicNotFoundResponse(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) {
        String body = readBody(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        if (epic.getId() != 0) {
            manager.updateEpic(epic);
            sendResponse(exchange, "Epic updated", 201);
        } else {
            manager.createEpic(epic);
            sendResponse(exchange, "Epic created", 201);
        }
    }

    private void handleDelete(HttpExchange exchange) {
        Optional<Long> idOpt = getTaskIdFromPath(exchange);
        if (idOpt.isPresent()) {
            manager.deleteEpic(idOpt.get());
            sendResponse(exchange, "Epic deleted", 200);
        } else {
            sendResponse(exchange, "ID is required", 400);
        }
    }

    private void sendEpicNotFoundResponse(HttpExchange exchange) {
        sendNotFoundResponse(exchange, "Epic not found");
    }

}