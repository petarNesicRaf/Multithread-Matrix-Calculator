package org.example.tasks;

public class PoisonTask implements Task{
    @Override
    public TaskType getType() {
        return TaskType.POISON;
    }
}
