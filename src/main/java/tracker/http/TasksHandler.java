package tracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.config.GsonConfig;
import tracker.managers.TaskManager;
import tracker.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager) {
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
                        sendText(exchange, gson.toJson(manager.getAllTasks()), 200);
                    } else if (pathParts.length == 3) {
                        long id = Long.parseLong(pathParts[2]);
                        Task task = manager.getTask(id);
                        if (task != null) sendText(exchange, gson.toJson(task), 200);
                        else sendNotFound(exchange);
                    }
                    break;

                case "POST":
                    InputStream inputStream = exchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    Task task = gson.fromJson(body, Task.class);

                    try {
                        if (task.getId() != 0) {
                            manager.updateTask(task);
                            sendText(exchange, "Updated", 201);
                        } else {
                            manager.createTask(task);
                            sendText(exchange, "Created", 201);
                        }
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                    }
                    break;

                case "DELETE":
                    if (pathParts.length == 3) {
                        long id = Long.parseLong(pathParts[2]);
                        manager.deleteTask(id);
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