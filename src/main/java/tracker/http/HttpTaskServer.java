package tracker.http;

import com.sun.net.httpserver.HttpServer;
import tracker.http.hadlers.*;
import tracker.managers.TaskManager;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-server started on port: " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-server stopped");
    }

}