package managers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InMemoryTaskManagerTest {
    @Test
    @DisplayName("Если закрыты все таски в epic'е, то эпик закрывается")
    public void closeAllTasks() {
        TaskManager taskManager = Managers.getDefault();

        Epic epic1 = taskManager.createEpic(new Epic("Ремонт", "В ванной"));

        Subtask task1 = taskManager.createSubtask(new Subtask("Подготовка", "Вынести мусор", epic1.getId()));
        Subtask task2 = taskManager.createSubtask(new Subtask("Покупки", "Плитка", epic1.getId()));

        task1.setStatus(Status.DONE);
        taskManager.updateSubtask(task1);

        task2.setStatus(Status.DONE);
        taskManager.updateSubtask(task2);

        assertThat(epic1.getStatus())
                .as("Ожидалось изменение статуса эпика на " + Status.DONE.name() + " при закрытии всех задач")
                .isEqualTo(Status.DONE);
    }

    @Test
    public void addTask() {
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task(1L, "name", "desc", Status.NEW);

        taskManager.createTask(task);

        assertThat(taskManager.getTask(1L))
                .isEqualTo(task);
    }

    @Test
    public void addTasksWithGeneratedAndManualId() {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task(1L, "name", "desc", Status.NEW);
        Task task2 = new Task("name", "desc");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

            assertAll(
                () ->
                        assertThat(taskManager.getTask(1L))
                                .isEqualTo(task1),
                () -> assertThat(taskManager.getTask(task2.getId()))
                        .isEqualTo(task2)
        );

    }
}
