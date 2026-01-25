package tracker.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.config.GsonConfig;
import tracker.managers.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = GsonConfig.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            sendText(exchange, gson.toJson(manager.getHistory()), 200);
        } else {
            exchange.sendResponseHeaders(405, 0);
        }
        exchange.close();
    }
}