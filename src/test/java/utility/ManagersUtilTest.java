package utility;

import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.managers.InMemoryTaskManager;
import tracker.utility.Managers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ManagersUtilTest {
    @Test
    public void smoke(){
        assertThat(Managers.getDefault())
                .isInstanceOf(InMemoryTaskManager.class);
    }
    @Test
    public void smokeHistory(){
        assertThat(Managers.getDefaultHistory())
                .isInstanceOf(InMemoryHistoryManager.class);
    }
}
