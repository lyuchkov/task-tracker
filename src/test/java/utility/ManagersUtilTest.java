package utility;

import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.managers.InMemoryTaskManager;
import tracker.utility.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ManagersUtilTest {
    @Test
    public void smoke() {
        assertEquals(InMemoryTaskManager.class, Managers.getDefault().getClass());
    }

    @Test
    public void smokeHistory() {
        assertEquals(InMemoryHistoryManager.class, Managers.getDefaultHistory().getClass());
    }
}
