package tracker;

import tracker.managers.TaskManager;
import tracker.tasks.Task;
import tracker.utility.Managers;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        TaskManager fileBacked = Managers.getFileBacked(new File("src/main/resources/file/backed/data.csv"));

        fileBacked.createTask(new Task("123", "12453"));
        fileBacked.createTask(new Task("123", "12453"));

    }
}
