package tracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.config.GsonConfig;
import tracker.managers.TaskManager;
import tracker.tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = GsonConfig.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        try {
            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        sendText(exchange, gson.toJson(manager.getAllSubtasks()), 200);
                    } else if (pathParts.length == 3) {
                        long id = Long.parseLong(pathParts[2]);
                        Subtask subtask = manager.getSubtask(id);
                        if (subtask != null) sendText(exchange, gson.toJson(subtask), 200);
                        else sendNotFound(exchange);
                    }
                    break;

                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    try {
                        if (subtask.getId() != 0) {
                            manager.updateSubtask(subtask);
                            sendText(exchange, "Updated", 201);
                        } else {
                            manager.createSubtask(subtask);
                            sendText(exchange, "Created", 201);
                        }
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                    }
                    break;

                case "DELETE":
                    if (pathParts.length == 3) {
                        long id = Long.parseLong(pathParts[2]);
                        manager.deleteSubtask(id);
                        sendText(exchange, "Deleted", 200);
                    }
                    break;

                default:
                    exchange.sendResponseHeaders(405, 0);
            }
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }
}