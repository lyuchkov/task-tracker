package managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.managers.InMemoryTaskManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InMemoryPriorityTaskManagerTest extends TaskManagerDurationTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = manager.createEpic(new Epic("E", "D"));
        manager.createSubtask(new Subtask(1, "S1", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now()));
        manager.createSubtask(new Subtask(2, "S2", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now().plusHours(1)));

        assertThat(epic.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = manager.createEpic(new Epic("E", "D"));
        Subtask s1 = manager.createSubtask(new Subtask(1L, "S1", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now()));
        Subtask s2 = manager.createSubtask(new Subtask(2L, "S2", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now().plusHours(1)));

        s1.setStatus(Status.DONE);
        s2.setStatus(Status.DONE);
        manager.updateSubtask(s1);
        manager.updateSubtask(s2);

        assertThat(epic.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void epicStatusShouldBeInProgressWhenNewAndDoneMixed() {
        Epic epic = manager.createEpic(new Epic("E", "D"));
        Subtask s1 = manager.createSubtask(new Subtask(1, "S1", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now()));
        manager.createSubtask(new Subtask(2, "S2", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now().plusHours(1)));

        s1.setStatus(Status.DONE);
        manager.updateSubtask(s1);

        assertThat(epic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }

    @Test
    void epicStatusShouldBeInProgressWhenAllInProgress() {
        Epic epic = manager.createEpic(new Epic("E", "D"));
        Subtask s1 = manager.createSubtask(new Subtask(1, "S1", "D", epic.getId(), Duration.ofMinutes(10), LocalDateTime.now()));

        s1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(s1);

        assertThat(epic.getStatus()).isEqualTo(Status.IN_PROGRESS);
    }
}