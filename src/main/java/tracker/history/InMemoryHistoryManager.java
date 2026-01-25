package tracker.history;

import tracker.tasks.Task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        history.add(new Task(task));

        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    public void addAll(Collection<Task> taskCollection) {
        if (taskCollection == null) {
            return;
        }
        if (history.size() + taskCollection.size() >= MAX_HISTORY_SIZE) {
            history.subList(0, Math.abs(MAX_HISTORY_SIZE - history.size() - taskCollection.size())).clear();
        }

        history.addAll(taskCollection.stream().map(Task::new).toList());
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(history);
    }
}