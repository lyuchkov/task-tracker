package tracker.http.hadlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.config.GsonConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final Gson gson = GsonConfig.getGson();

    protected void sendResponse(HttpExchange h, String text, int rCode) {
        try (h) {
            byte[] resp = text.getBytes(StandardCharsets.UTF_8);
            h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            h.sendResponseHeaders(rCode, resp.length);
            h.getResponseBody().write(resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendNotFoundResponse(HttpExchange exchange, String message) {
        sendResponse(exchange, gson.toJson(message), 404);
    }

    protected void sendHasInteractions(HttpExchange exchange, String message) {
        sendResponse(exchange, gson.toJson(message), 406);
    }

    protected void sendInternalError(HttpExchange exchange, String message) {
        sendResponse(exchange, gson.toJson(message), 500);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) {
        sendResponse(exchange, gson.toJson("Method Not Allowed"), 405);
    }

    protected Optional<Long> getTaskIdFromPath(HttpExchange exchange) {
        String[] parts = exchange.getRequestURI().getPath().split("/");
        try {
            if (parts.length >= 3) {
                return Optional.of(Long.parseLong(parts[2]));
            }
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    protected String readBody(HttpExchange h) {
        try {
            return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}