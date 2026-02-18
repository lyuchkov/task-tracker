package tracker.http.hadlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.managers.TaskManager;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if ("GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
        } else {
            sendResponse(exchange, "Method Not Allowed", 405);
        }
    }
}