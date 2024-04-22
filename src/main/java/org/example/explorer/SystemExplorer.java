package org.example.explorer;

import org.example.cli.MainCLI;
import org.example.model.Matrix;
import org.example.queue.TaskQueue;
import org.example.tasks.CreateTask;
import org.example.tasks.RemoveTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class SystemExplorer implements Runnable {
    private final TaskQueue taskQueue;
    private final List<String> roots;
    private final Map<File, Long> timeModifiedCache;
    private boolean working = true;
    public SystemExplorer(TaskQueue taskQueue) {
        this.taskQueue = taskQueue;
        this.roots = new CopyOnWriteArrayList<>();
        this.timeModifiedCache = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        while (working) {
            try {
                explore();
                sleep(MainCLI.SLEEP_TIME);//todo properties
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void explore() {
        try {
            for (String folderPath : this.roots) {
                File f = new File(folderPath);
                if(!f.exists()){
                    System.out.println("File|folder " + f.getAbsolutePath()+" doesn't exist.");
                    this.roots.remove(folderPath);
                    break;
                }
                Path p = Paths.get(folderPath);
                List<File> matrixFiles = Files.walk(p)
                        .filter(path -> path.toString().endsWith(".rix"))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                matrixFiles.forEach(file ->
                {
                    if (timeModifiedCache.containsKey(file)) {
                        if (timeModifiedCache.get(file) != file.lastModified()) {
                            System.out.println("File " + file.getName() + " has been modified.");
                            try {
                                String matrixName = getMatrixName(file);
                                if(matrixName != null)
                                {
                                    System.out.println("Removing file..." + file.getName());
                                    taskQueue.add(new RemoveTask(matrixName, file));
                                    timeModifiedCache.replace(file, file.lastModified());
                                }
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        try {
                            System.out.println("Found matrix " + file.getName());
                            taskQueue.add(new CreateTask(file));
                            timeModifiedCache.put(file, file.lastModified());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getMatrixName(File f){
        try (BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath()))) {
            String firstLine = reader.readLine();
            String[] matrixInfo = firstLine.split(", ");
            String matrixName = matrixInfo[0].split("=")[1];
            return matrixName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void poison() {
        this.working = false;
        System.out.println("System explorer shutting down...");
    }

    public TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public List<String> getRoots() {
        return roots;
    }


    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }


}
