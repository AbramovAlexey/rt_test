package main;

import main.config.ThreadConfig;
import main.config.WorkConfig;

import java.util.List;

public class Application {

    public static void main(String[] args) {
        var workConfig = new WorkConfig(5, 2, List.of(new ThreadConfig(10, 3),
                                                                           new ThreadConfig(5, 2)));
        var threadManager = new ThreadManager(workConfig);
        threadManager.doWork();
    }

}
