package tracker.managers;

import tracker.history.HistoryManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static tracker.utility.IdUtils.getIdByString;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Long, Task> tasks = new HashMap<>();
    protected final HashMap<Long, Epic> epics = new HashMap<>();
    protected final HashMap<Long, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task createTask(Task task) {
        task.setId(task.getId() == 0L ? getIdByString(task.toString()) : task.getId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public Task getTask(long id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public void deleteTask(long id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getIdByString(epic.getName()));
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic savedEpic = epics.get(epic.getId());
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        }
    }

    @Override
    public Epic getEpic(long id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public void deleteEpic(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Long subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null;
        }
        subtask.setId(getIdByString(subtask.getName()));
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);

        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public Subtask getSubtask(long id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void deleteSubtask(long id) {
        Subtask subtask = subtasks.remove(id);
        historyManager.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(long epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Long subtaskId : epic.getSubtaskIds()) {
                result.add(subtasks.get(subtaskId));
            }
        }
        return result;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> subtasks = new ArrayList<>(this.subtasks.values());
        subtasks.forEach(historyManager::add);
        return subtasks;
    }

    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> epics = new ArrayList<>(this.epics.values());
        epics.forEach(historyManager::add);
        return epics;
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> tasks = new ArrayList<>(this.tasks.values());
        tasks.forEach(historyManager::add);
        return tasks;
    }

   public void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        boolean allDone = true;
        boolean allNew = true;
        for (Long subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStatus() != Status.DONE) allDone = false;
            if (subtask.getStatus() != Status.NEW) allNew = false;
        }
        if (allDone) epic.setStatus(Status.DONE);
        else if (allNew) epic.setStatus(Status.NEW);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}