package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.config.GsonConfig;
import tracker.http.HttpTaskServer;
import tracker.managers.InMemoryTaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpTaskManagerTest {
    private HttpTaskServer taskServer;
    private InMemoryTaskManager manager;
    private final Gson gson = GsonConfig.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();

    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws Exception {
        Task task = new Task("Test Task", "Test task creation", Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);

        List<Task> tasksFromManager = manager.getAllTasks();
        assertThat(tasksFromManager).hasSize(1);
        assertThat(tasksFromManager.getFirst().getName()).isEqualTo("Test Task");
    }

    @Test
    public void testAddTaskOverlap() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 12, 0);
        manager.createTask(new Task("Existing", "Desc", Duration.ofMinutes(60), start));

        Task newTask = new Task("New", "Desc", Duration.ofMinutes(60), start.plusMinutes(30));
        String taskJson = gson.toJson(newTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(406);
    }

    @Test
    public void testUpdateTask() throws Exception {
        Task task = manager.createTask(new Task("Old Name", "Desc"));

        Task taskToUpdate = new Task("New Name", "Desc");
        taskToUpdate.setId(task.getId());

        String taskJson = gson.toJson(taskToUpdate);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(manager.getTask(task.getId()).getName()).isEqualTo("New Name");
    }

    @Test
    public void testDeleteTask() throws Exception {
        Task task = manager.createTask(new Task("To Delete", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(manager.getAllTasks()).isEmpty();
    }

    @Test
    public void testGetTaskById() throws Exception {
        Task task = manager.createTask(new Task("Test", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertThat(receivedTask.getId()).isEqualTo(task.getId());
    }

    @Test
    public void testGetTaskNotFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
    }

    @Test
    public void testAddSubtask() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId(), Duration.ofMinutes(15),
                LocalDateTime.now());
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(manager.getAllSubtasks()).hasSize(1);
        assertThat(manager.getEpic(epic.getId()).getSubtaskIds()).hasSize(1);
    }

    @Test
    public void testGetEpicSubtasks() throws Exception {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub = manager.createSubtask(new Subtask(1L, "Sub", "Desc", epic.getId(),
                Duration.ofMinutes(10), LocalDateTime.now()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {}.getType());

        assertThat(subtasks).hasSize(1);
        assertThat(subtasks.getFirst().getId()).isEqualTo(sub.getId());
    }

    @Test
    public void testGetHistory() throws Exception {
        Task t1 = manager.createTask(new Task("T1", "D"));
        Task t2 = manager.createTask(new Task("T2", "D"));

        manager.getTask(t1.getId());
        manager.getTask(t2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        List<Task> history =
                gson.fromJson(response.body(), new TypeToken<List<Task>>() {
                }.getType());
        assertThat(history).hasSize(2);
        assertThat(history.getFirst().getId()).isEqualTo(t1.getId());
    }

    @Test
    public void testGetPrioritized() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        manager.createTask(new Task("Late", "Desc", Duration.ofMinutes(10), now.plusHours(1)));

        manager.createTask(new Task("Early", "Desc", Duration.ofMinutes(10), now));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());

        assertThat(prioritized).hasSize(2);
        assertThat(prioritized.getFirst().getName()).isEqualTo("Early");
    }
}