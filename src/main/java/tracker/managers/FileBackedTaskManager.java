package tracker.managers;

import tracker.managers.exceptions.ManagerSaveException;
import tracker.tasks.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + file.getName(), e);
        }
    }

    private String toString(Task task) {
        String type =  TaskType.TASK.name();
        String epicId = "";

        if (task instanceof Epic) {
            type = TaskType.EPIC.name();
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK.name();
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    public FileBackedTaskManager loadFromFile() {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        long maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine();

            while (reader.ready()) {
                String line = reader.readLine();
                if (line.isEmpty()) break;

                Task task = fromString(line);
                long id = task.getId();

                if (id > maxId) maxId = id;

                addAnyTask(manager, task);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла: " + file.getName(), e);
        }

        manager.restoreEpicSubtasks();

        return manager;
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        long id = Long.parseLong(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        return switch (type) {
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> {
                long epicId = Integer.parseInt(parts[5]);
                yield new Subtask(id, name, description, status, epicId);
            }
            default -> new Task(id, name, description, status);
        };
    }

    private void addAnyTask(FileBackedTaskManager manager, Task task) {
        if (task instanceof Epic) {
            manager.addEpicToMap((Epic) task);
        } else if (task instanceof Subtask) {
            manager.addSubtaskToMap((Subtask) task);
        } else {
            manager.addTaskToMap(task);
        }
    }

    private void addTaskToMap(Task task) {
        tasks.put(task.getId(), task);
    }

    private void addEpicToMap(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    private void addSubtaskToMap(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    private void restoreEpicSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtaskId(subtask.getId());
            }
        }

        for (Epic epic : epics.values()) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public Task createTask(Task task) {
        Task t = super.createTask(task);
        save();
        return t;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(long id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic e = super.createEpic(epic);
        save();
        return e;
    }

    @Override
    public void deleteEpic(long id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask s = super.createSubtask(subtask);
        save();
        return s;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(long id) {
        super.deleteSubtask(id);
        save();
    }
}