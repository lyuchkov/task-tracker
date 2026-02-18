package tracker.http.hadlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.managers.TaskManager;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if ("GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, gson.toJson(manager.getHistory()), 200);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }
}