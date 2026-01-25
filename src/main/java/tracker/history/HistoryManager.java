package tracker.history;

import tracker.tasks.Task;

import java.util.Collection;
import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void addAll(Collection<Task> taskCollection);
    List<Task> getHistory();
}