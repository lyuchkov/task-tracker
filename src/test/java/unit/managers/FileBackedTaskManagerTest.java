package unit.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.managers.FileBackedTaskManager;
import tracker.managers.exceptions.ManagerSaveException;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    public void save_saveCorrectly_emptyTasks() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method saveMethod = FileBackedTaskManager.class.getDeclaredMethod("save");

        saveMethod.setAccessible(true);
        saveMethod.invoke(manager);
        FileBackedTaskManager loadedManager =  new FileBackedTaskManager(tempFile);

        assertThat(loadedManager.getAllTasks()).isEmpty();
        assertThat(loadedManager.getAllEpics()).isEmpty();
        assertThat(loadedManager.getAllSubtasks()).isEmpty();
    }

    @Test
    public void testSaveMultipleTasks() throws IOException {
        Task task = new Task("Task", "Desc");
        manager.createTask(task);

        Epic epic = new Epic("Epic", "Desc");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        manager.createSubtask(subtask);

        List<String> lines = Files.readAllLines(tempFile.toPath());

        assertThat(lines).hasSize(4);
        assertThat(lines.getFirst()).contains("id", "type", "name", "status", "description", "epic");
        assertThat(lines).anyMatch(l -> l.contains("TASK"));
        assertThat(lines).anyMatch(l -> l.contains("EPIC"));
        assertThat(lines).anyMatch(l -> l.contains("SUBTASK"));
    }

    @Test
    public void testLoadMultipleTasks() {
        Task task = new Task("T1", "D1");
        long taskId = manager.createTask(task).getId();

        Epic epic = new Epic("E1", "D1");
        long epicId = manager.createEpic(epic).getId();

        Subtask subtask = new Subtask("S1", "D1", epicId);
        long subId = manager.createSubtask(subtask).getId();

        FileBackedTaskManager loadedManager =  new FileBackedTaskManager(tempFile);

        assertThat(loadedManager.getAllTasks()).hasSize(1);
        assertThat(loadedManager.getAllEpics()).hasSize(1);
        assertThat(loadedManager.getAllSubtasks()).hasSize(1);

        assertThat(loadedManager.getTask(taskId)).extracting(Task::getName).isEqualTo("T1");
        assertThat(loadedManager.getEpic(epicId)).extracting(Epic::getName).isEqualTo("E1");
        assertThat(loadedManager.getSubtask(subId))
                .satisfies(s -> {
                    assertThat(s.getName()).isEqualTo("S1");
                    assertThat(s.getEpicId()).isEqualTo(epicId);
                });
    }

    @Test
    public void testEpicStatusRestoration() {
        Epic epic = new Epic("Epic", "Desc");
        long epicId = manager.createEpic(epic).getId();

        Subtask subtask = new Subtask("Sub", "Desc", epicId);
        subtask.setStatus(Status.DONE);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager =  new FileBackedTaskManager(tempFile);

        assertThat(loadedManager.getEpic(epicId).getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    public void testExceptionOnInvalidFile() {
        File invalidFile = new File("/non/existent/path/file.csv");

        assertThatThrownBy(() -> new FileBackedTaskManager(invalidFile))
                .isInstanceOf(ManagerSaveException.class);
    }
}