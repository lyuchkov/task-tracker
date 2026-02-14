package tracker.utility;

import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;
import tracker.managers.InMemoryTaskManager;
import tracker.managers.TaskManager;

public final class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
