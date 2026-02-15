package unit.managers;

import org.junit.jupiter.api.Test;
import tracker.managers.TaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Subtask;
import tracker.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public abstract class TaskManagerDurationTest <T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @Test
    void shouldCreateTaskAndGetById() {
        Task task = new Task("Task", "Desc",  Duration.ofMinutes(30), LocalDateTime.now());
        long id = manager.createTask(task).getId();

        Task saved = manager.getTask(id);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Task");
    }

    @Test
    void shouldCreateSubtaskWithEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask subtask = new Subtask(1L, "Sub", "Desc", epic.getId(),  Duration.ofMinutes(15),LocalDateTime.now());

        manager.createSubtask(subtask);

        assertThat(manager.getSubtask(subtask.getId())).isNotNull();
        assertThat(manager.getSubtask(subtask.getId()).getEpicId()).isEqualTo(epic.getId());
        assertThat(epic.getSubtaskIds().contains(subtask.getId())).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenTasksOverlap() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        manager.createTask(new Task("T1", "D",Duration.ofMinutes(60), start));

        Task overlappingTask = new Task("T2", "D",  Duration.ofMinutes(60),start.plusMinutes(30));

        assertThatThrownBy(() -> manager.createTask(overlappingTask))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
