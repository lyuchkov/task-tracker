package history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
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
                        .isEqualTo(2),

                () -> assertThat(history.getFirst().getDescription())
                        .isEqualTo(DESC),

                () -> assertThat(history.getLast().getDescription())
                        .isEqualTo(NEWDESC)
        );
    }


    @Test
    public void testSaveAndLoadWithTime() {
        TaskManager manager = Managers.getDefault();
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        Task task = new Task("TimeTask", "Desc", Duration.ofMinutes(30), start);

        manager.createTask(task);

        Task loadedTask = manager.getTask(task.getId());

        assertThat(loadedTask.getStartTime()).isEqualTo(start);
        assertThat(loadedTask.getDuration().toMinutes()).isEqualTo(30);
        assertThat(loadedTask.getEndTime()).isEqualTo(start.plusMinutes(30));
    }

    @Test
    public void testEpicTimeCalculation() {
        TaskManager manager = Managers.getDefault();
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        LocalDateTime start1 = LocalDateTime.of(2026, 1, 1, 10, 0);
        Subtask sub1 = new Subtask(1, "S1", "D", epic.getId(), Duration.ofMinutes(60), start1);
        manager.createSubtask(sub1);

        LocalDateTime start2 = LocalDateTime.of(2026, 1, 1, 12, 0);
        Subtask sub2 = new Subtask(2, "S2", "D", epic.getId(), Duration.ofMinutes(30), start2);
        manager.createSubtask(sub2);

        assertThat(epic.getStartTime()).isEqualTo(start1);
        assertThat(epic.getEndTime()).isEqualTo(start2.plusMinutes(30));
        assertThat(epic.getDuration().toMinutes()).isEqualTo(90);

        Epic loadedEpic = manager.getEpic(epic.getId());

        assertThat(loadedEpic.getStartTime()).isEqualTo(start1);
        assertThat(loadedEpic.getEndTime()).isEqualTo(start2.plusMinutes(30));
        assertThat(loadedEpic.getDuration().toMinutes()).isEqualTo(90);
    }
}
