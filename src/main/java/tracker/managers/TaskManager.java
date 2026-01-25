package tracker.managers;

import tracker.tasks.Epic;
import tracker.tasks.Subtask;
import tracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface TaskManager {
    Task createTask(Task task);

    void updateTask(Task task);

    Task getTask(long id);

    ArrayList<Task> getAllTasks();

    void deleteTask(long id);

    void deleteAllTasks();

    Epic createEpic(Epic epic);

    void updateEpic(Epic epic);

    Epic getEpic(long id);

    ArrayList<Epic> getAllEpics();

    void deleteEpic(long id);

    void deleteAllEpics();

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    Subtask getSubtask(long id);

    ArrayList<Subtask> getAllSubtasks();

    void deleteSubtask(long id);

    void deleteAllSubtasks();

    ArrayList<Subtask> getEpicSubtasks(long epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}