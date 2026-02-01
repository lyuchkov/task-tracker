package tasks;

import org.junit.jupiter.api.Test;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {

    @Test
    public void taskEqualTest() {
        Task task1 = new Task(
                1L,
                "name",
                "desc",
                Status.NEW
        );
        Task task2 = new Task(
                1L,
                "name1",
                "desc2",
                Status.DONE
        );

        assertEquals(task1, task2);
    }

    @Test
    public void tasksWithDifClassesEqualTest() {
        Epic task1 = new Epic(
                1L,
                "name",
                "desc"
        );
        Subtask task2 = new Subtask(
                1L,
                "name1",
                "desc2",
                1L
        );

        assertEquals(task1, task2);
    }
}
