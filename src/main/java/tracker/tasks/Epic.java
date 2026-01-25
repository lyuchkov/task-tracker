package tracker.tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Long> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(long id, String name, String description) {
        super(id, name, description, Status.NEW);
    }

    public void addSubtaskId(long id) {
        if(id == this.getId()) throw new IllegalArgumentException("input id equals to epic id");
        subtaskIds.add(id);
    }

    public void removeSubtaskId(long id) {
        subtaskIds.remove(id);
    }

    public ArrayList<Long> getSubtaskIds() {
        return subtaskIds;
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}