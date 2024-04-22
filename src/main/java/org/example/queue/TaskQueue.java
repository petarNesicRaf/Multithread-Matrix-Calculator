package org.example.queue;

import org.example.tasks.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {
    private final BlockingQueue<Task> taskQueue;

    public TaskQueue() {
        this.taskQueue = new LinkedBlockingQueue<>();
    }
    public void add(Task task) throws InterruptedException {
        this.taskQueue.put(task);
    }

    public Task take() throws InterruptedException {
        return this.taskQueue.take();
    }
}
