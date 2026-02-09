package unit.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.tasks.Status;
import tracker.tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    public static final String DESC = "desc";

    @Test
    public void getTaskFromHistoryManagerTest() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        var id = 1L;
        Task task = new Task(id, "name", DESC, Status.NEW);

        manager.add(task);

        assertTrue(manager.getHistory().stream().map(Task::getId).toList().contains(id));
    }


    @Test
    public void removeElementFromInMemoryHistoryManager() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        var id = 1L;
        Task task = new Task(id, "name", DESC, Status.NEW);

        manager.add(task);

        manager.remove(id);

        assertFalse(manager.getHistory().stream().map(Task::getId).toList().contains(id));
    }


    @Test
    @DisplayName("Проверка порядка добавления тасок в историю")
    public void inMemoryHistoryManagerAdditionOrder() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();

        var id1 = 1L;
        var id2 = 2L;
        Task task1 = new Task(id1, "name", DESC, Status.NEW);
        Task task2 = new Task(id2, "name", DESC, Status.NEW);

        manager.add(task1);
        manager.add(task2);


        assertAll(
                () -> assertEquals(manager.getHistory().getFirst(), task1),
                () -> assertEquals(manager.getHistory().getLast(), task2)
        );
    }

}
