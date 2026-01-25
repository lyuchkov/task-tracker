package history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InMemoryHistoryManagerTest {

    public static final String NEWDESC = "newdesc";
    public static final String DESC = "desc";

    @Test
    @DisplayName("Проверка истории. Методы getAllEpics и getSubTask")
    public void closeAllTasks() {
        TaskManager taskManager = Managers.getDefault();

        Epic epic1 = taskManager.createEpic(new Epic("Ремонт", "В ванной"));

        Subtask task1 = taskManager.createSubtask(new Subtask("Подготовка", "Вынести мусор", epic1.getId()));
        Subtask task2 = taskManager.createSubtask(new Subtask("Покупки", "Плитка", epic1.getId()));

        task1.setStatus(Status.DONE);
        taskManager.updateSubtask(task1);

        task2.setStatus(Status.DONE);
        taskManager.updateSubtask(task2);

        taskManager.getAllEpics();
        taskManager.getSubtask(task2.getId());

        List<Task> history = taskManager.getHistory();

        assertAll(
                () -> assertThat(history.size())
                        .isEqualTo(2),

                () -> assertThat(history.getFirst().getId())
                        .isEqualTo(epic1.getId()),

                () -> assertThat(history.getLast().getId())
                        .isEqualTo(task2.getId())

        );
    }

    @Test
    public void historyWithModifiedTask() {
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task("name", DESC);

        taskManager.createTask(task);
        taskManager.getTask(task.getId());

        task.setDescription(NEWDESC);
        taskManager.updateTask(task);

        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();

        assertAll(
                () -> assertThat(history.size())
                        .isEqualTo(1),

                () -> assertThat(history.getFirst().getDescription())
                        .isEqualTo(NEWDESC)
        );
    }


    @Test
    public void remove() {
        TaskManager taskManager = Managers.getDefault();

        Task task = new Task("name", DESC);

        taskManager.createTask(task);
        taskManager.getTask(task.getId());

        task.setDescription(NEWDESC);
        taskManager.updateTask(task);

        taskManager.getTask(task.getId());

        taskManager.deleteTask(task.getId());

        List<Task> history = taskManager.getHistory();

        assertAll(
                () -> assertThat(history.size())
                        .isEqualTo(0)
        );
    }

}
