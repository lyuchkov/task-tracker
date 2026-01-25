package tracker.http;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.config.GsonConfig;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager) {
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
                        sendText(exchange, gson.toJson(manager.getAllEpics()), 200);
                    } else if (pathParts.length == 3) {
                        long id = Long.parseLong(pathParts[2]);
                        Epic epic = manager.getEpic(id);
                        if (epic != null) sendText(exchange, gson.toJson(epic), 200);
                        else sendNotFound(exchange);
                    } else if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                        long id = Long.parseLong(pathParts[2]);
                        Epic epic = manager.getEpic(id);
                        if (epic != null) {
                            sendText(exchange, gson.toJson(manager.getEpicSubtasks(id)), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    }
                    break;

                case "POST":
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Epic epic = gson.fromJson(body, Epic.class);
                    if (epic.getId() != 0) {
                        manager.updateEpic(epic);
                    } else {
                        manager.createEpic(epic);
                    }
                    sendText(exchange, "Success", 201);
                    break;

                case "DELETE":
                    if (pathParts.length == 3) {
                        manager.deleteEpic(Long.parseLong(pathParts[2]));
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
