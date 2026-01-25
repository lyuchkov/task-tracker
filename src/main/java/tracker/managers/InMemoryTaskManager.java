package tracker.managers;

import tracker.history.HistoryManager;
import tracker.tasks.Epic;
import tracker.tasks.Status;
import tracker.tasks.Subtask;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static tracker.utility.IdUtils.getIdByString;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Long, Task> tasks = new HashMap<>();
    private final HashMap<Long, Epic> epics = new HashMap<>();
    private final HashMap<Long, Subtask> subtasks = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
    ).thenComparingLong(Task::getId));

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .toList();
    }

    @Override
    public Task createTask(Task task) {
        validateIntersections(task);
        task.setId(task.getId() == 0L ? getIdByString(task.toString()) : task.getId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public void updateTask(Task task) {
        Task saved = tasks.get(task.getId());
        if (saved == null) return;

        validateIntersections(task);
        prioritizedTasks.remove(saved);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
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
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> tasks = new ArrayList<>(this.tasks.values());
        historyManager.addAll(tasks);
        return tasks;
    }

    @Override
    public void deleteTask(long id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Epic createEpic(Epic epic) {
        validateIntersections(epic);
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
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> epics = new ArrayList<>(this.epics.values());
        historyManager.addAll(new ArrayList<>(epics));
        return epics;
    }

    @Override
    public void deleteEpic(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Long subId : epic.getSubtaskIds()) {
                Subtask sub = subtasks.remove(subId);
                prioritizedTasks.remove(sub);
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
        validateIntersections(subtask);
        subtask.setId(getIdByString(subtask.getName()));
        subtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        updateEpicTime(epic);
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Subtask saved = subtasks.get(subtask.getId());
        if (saved == null) return;

        validateIntersections(subtask);
        prioritizedTasks.remove(saved);
        subtasks.put(subtask.getId(), subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        updateEpicStatus(epic);
        updateEpicTime(epic);
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
    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> subtasks = new ArrayList<>(this.subtasks.values());
        historyManager.addAll(new ArrayList<>(subtasks));
        return subtasks;
    }
    @Override
    public void deleteSubtask(long id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtaskId(id);
            updateEpicStatus(epic);
            updateEpicTime(epic);
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

    private void updateEpicStatus(Epic epic) {
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

        updateEpicTime(epic);
    }

    private void updateEpicTime(Epic epic) {
        List<Long> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        LocalDateTime minStart = null;
        LocalDateTime maxEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Long id : subs) {
            Subtask subtask = subtasks.get(id);
            if (subtask == null) continue;

            if (subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }

            LocalDateTime subStart = subtask.getStartTime();
            if (subStart != null) {
                if (minStart == null || subStart.isBefore(minStart)) {
                    minStart = subStart;
                }
            }

            LocalDateTime subEnd = subtask.getEndTime();
            if (subEnd != null) {
                if (maxEnd == null || subEnd.isAfter(maxEnd)) {
                    maxEnd = subEnd;
                }
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(minStart);
        epic.setEndTime(maxEnd);
    }

    private boolean isOverlap(Task t1, Task t2) {
        if (t1.getStartTime() == null || t2.getStartTime() == null) {
            return false;
        }

        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private void validateIntersections(Task task) {
        if (task.getStartTime() == null) {
            return;
        }

        boolean hasOverlap = prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> t.getStartTime() != null)
                .anyMatch(t -> isOverlap(task, t));

        if (hasOverlap) {
            throw new IllegalArgumentException("Задача пересекается по времени с уже существующими");
        }
    }
}