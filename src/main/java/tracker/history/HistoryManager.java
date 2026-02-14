package tracker.history;

import tracker.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    void remove(long id);

    List<Task> getHistory();
}