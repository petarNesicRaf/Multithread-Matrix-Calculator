package org.example.coordinator;

import org.example.brain.MatrixBrain;
import org.example.model.Matrix;
import org.example.mulextr.MatrixExtractor;
import org.example.mulextr.MatrixMultiplier;
import org.example.queue.TaskQueue;
import org.example.tasks.CreateTask;
import org.example.tasks.MultiplyTask;
import org.example.tasks.RemoveTask;
import org.example.tasks.Task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskCoordinator extends Thread{
    private TaskQueue taskQueue;
    private MatrixMultiplier matrixMultiplier;
    private MatrixExtractor matrixExtractor;
    private MatrixBrain matrixBrain;
    private boolean working = true;

    public  TaskCoordinator(TaskQueue taskQueue, MatrixBrain matrixBrain)
    {
        this.taskQueue = taskQueue;
        this.matrixMultiplier = new MatrixMultiplier();
        this.matrixExtractor = new MatrixExtractor();
        this.matrixBrain = matrixBrain;
    }

    @Override
    public void run() {
        while(working){
            try{
                Task task = this.taskQueue.take();
                switch(task.getType()){
                    case CREATE:
                        CreateTask createTask = (CreateTask) task;
                        Future<Matrix> futureCreateResult =  matrixExtractor.getForkJoinPool().submit(createTask);
                        matrixBrain.addCreateTask(createTask, futureCreateResult);
                        Matrix matrix = futureCreateResult.get();
                        matrixBrain.getAllMatrixMap().putIfAbsent(matrix.getName(), matrix);
                        System.out.println("Matrix " + matrix.getName() + " created.");

                        MultiplyTask multiplyTask = new MultiplyTask(matrix,matrix);
                        this.taskQueue.add(multiplyTask);

                        break;
                    case MULTIPLY:
                        MultiplyTask multiplyTaskk = (MultiplyTask) task;
                        Matrix a = multiplyTaskk.getA();
                        Matrix b = multiplyTaskk.getB();
                        if(a.getCols()!=b.getRows()) {
                            System.out.println("Matrices " + a.getName() +" and " +b.getName() + " can't be multiplied");
                            break;
                        }
                        Future<Matrix> futureMultiplyResult = matrixMultiplier.getForkJoinPool().submit(multiplyTaskk);
                        System.out.println("Calculating " + a.getName() + b.getName());
                        matrixBrain.addMultiplyTask(a,b, futureMultiplyResult);
                        matrixBrain.submitTask(a.getName()+" "+b.getName(), "multiply");
                        break;
                    case POISON:
                        poison();
                        break;
                    case REMOVE:
                        RemoveTask removeTask = (RemoveTask) task;
                        this.matrixBrain.clear(removeTask.getName());
                        this.taskQueue.add(new CreateTask(removeTask.getFile()));
                }
            }catch(InterruptedException e){
                throw  new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void poison() {
        matrixMultiplier.getForkJoinPool().shutdownNow();
        matrixExtractor.getForkJoinPool().shutdownNow();
        System.out.println("stopping coordinator...");
        working = false;
    }
}
