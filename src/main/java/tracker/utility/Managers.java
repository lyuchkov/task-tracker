package tracker.utility;

import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;
import tracker.managers.FileBackedTaskManager;
import tracker.managers.InMemoryTaskManager;
import tracker.managers.TaskManager;

import java.io.File;

public final class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileBacked(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
