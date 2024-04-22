package org.example.tasks;

import java.io.File;

public class RemoveTask implements Task{
    private String name;
    private File file;

    public RemoveTask(String name, File file) {
        this.name = name;
        this.file = file;
    }

    @Override
    public TaskType getType() {
        return TaskType.REMOVE;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
}
