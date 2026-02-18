package tracker;

import tracker.http.HttpTaskServer;
import tracker.utility.Managers;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(Managers.getDefault());
        taskServer.start();
    }
}
