package tracker.tasks;

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

    public long getEpicId() {
        return epicId;
    }

}
