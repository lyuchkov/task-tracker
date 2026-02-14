package tracker.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final long epicId;

    public Subtask(String name, String description, long epicId) {
        super(name, description);
        this.epicId = epicId;
    }


    public Subtask(long id, String name, String description, long epicId) {
        super(id, name, description, Status.NEW);
        this.epicId = epicId;
    }


    public Subtask(long id, String name, String description, long epicId, Duration duration, LocalDateTime start) {
        super(id, name, description, Status.NEW);
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = start;
    }

    public Subtask(long id, String name, String description, Status status, long epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

}
