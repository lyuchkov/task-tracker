package integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Проверка работы дефолтного сценария работы таск менеджера")
public class HistoryTest {

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

        taskManager.getEpic(epic1.getId());
        taskManager.getSubtask(task2.getId());

        List<Task> history = taskManager.getHistory();

        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertEquals(epic1.getId(), history.getFirst().getId()),
                () -> assertEquals(task2.getId(), history.getLast().getId())
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
                () -> assertEquals(1, history.size()),

                () -> assertEquals(NEWDESC, history.getFirst().getDescription())
        );
    }

}
