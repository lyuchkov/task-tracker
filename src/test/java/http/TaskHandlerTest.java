package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tracker.config.GsonConfig;
import tracker.http.HttpTaskServer;
import tracker.managers.InMemoryTaskManager;
import tracker.managers.TaskManager;
import tracker.tasks.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskHandlerTest {
    private static HttpTaskServer taskServer;
    private static TaskManager manager;
    private final Gson gson = GsonConfig.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    public static void setUp() throws Exception {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
    }

    @AfterAll
    public static void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws Exception {
        Task task = new Task("Test", "Desc");
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(manager.getAllTasks()).anyMatch(task1 -> Objects.equals(task1.getName(), task.getName()) && Objects.equals(task1.getDescription(), task.getDescription()));
    }

    @Test
    public void testGetTasks() throws Exception {
        manager.createTask(new Task("T1", "D1"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertThat(tasks).hasSize(1);
    }

    @Test
    public void testGetTaskByIdNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
    }
}