package org.example.cli;

import org.example.brain.MatrixBrain;
import org.example.coordinator.TaskCoordinator;
import org.example.explorer.SystemExplorer;
import org.example.model.Matrix;
import org.example.queue.TaskQueue;
import org.example.tasks.MultiplyTask;
import org.example.tasks.PoisonTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class MainCLI {
    public static Map<String, String> configMap = new HashMap<>();
    private final TaskQueue taskQueue;
    private final SystemExplorer systemExplorer;
    private final MatrixBrain matrixBrain;
    private TaskCoordinator taskCoordinator;
    public static int SLEEP_TIME;
    public static int CHUNK_SIZE;
    public static int MAX_ROWS;
    public static String START_DIR;
    public MainCLI() {
        this.taskQueue = new TaskQueue();
        this.matrixBrain = new MatrixBrain();//16
        this.systemExplorer = new SystemExplorer(taskQueue);

        configure();
        init();
    }

    public void configure() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("./src/main/resources/app.properties")) {
            properties.load(fis);

            properties.forEach((key, value) -> {
                configMap.put((String) key, (String) value);
                System.out.println("key " + key + " value " + value);
            });

            SLEEP_TIME = Integer.parseInt(configMap.get("sys_explorer_sleep_time"));
            CHUNK_SIZE = Integer.parseInt(configMap.get("maximum_file_chunk_size"));
            MAX_ROWS = Integer.parseInt(configMap.get("maximum_rows_size"));
            START_DIR = configMap.get("start_dir");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        initComponents();

        Scanner s = new Scanner(System.in);
        while (true) {
            //System.out.println("Enter a command:");
            String line = s.nextLine().trim();
            String[] commandParam = line.split(" ");
            String command = commandParam[0];
            String params = "";
            if (commandParam.length == 2)
                params = commandParam[1];
            else params = parseParams(commandParam);

            int paramCount = commandParam.length - 1;

            switch (command) {
                case "dir":
                    if (paramCount == 1)
                        dir(params);
                    else System.out.println("Param count bad");
                    break;
                case "info":
                    if (paramCount == 1)
                        info(params);
                    else if (paramCount == 2)
                        infoN(params);
                    else System.out.println("Param count bad");
                    break;
                case "multiply":
                    if (paramCount == 3 && params.contains("-sync"))
                        multiplySync(params);
                    else if(paramCount == 3 && params.contains("-async"))
                        multiplyASync(params);
                    else System.out.println("Param count bad");
                    break;
                case "save":
                    if (paramCount == 4)
                        save(params);
                    else System.out.println("save todo");
                    break;
                case "clear":
                    if (paramCount == 1)
                        clear(params);
                    else System.out.println("Params are bad");
                    break;
                case "stop":
                    System.out.println("Stopping...");
                    poison();
                default:
                    System.out.println("invalid command");

            }
        }
    }

    public void infoN(String param) {
        String[] par = param.split(" ");
        String sf = par[0];
        String n = par[1];
        if ((sf.equals("-s") || sf.equals("-e")) && n.matches("-?\\d+(\\.\\d+)?")) {
            this.matrixBrain.submitTask(sf + "," + n, "info");
        } else {
            System.out.println("bad param");
        }
    }

    public void info(String param) {
        this.matrixBrain.submitTask(param, "info");
    }

    public void multiplySync(String param) {
        String[] matrixNames = param.split(" ");
        if (matrixNames.length != 3) {
            System.out.println("Matrix params are wrong or flag is missing");
            return;
        }
        String matrixName = (matrixNames[0] + matrixNames[1]).toUpperCase();
        Map<String, Matrix> cache = matrixBrain.getCacheMap();

        if (cache.containsKey(matrixName)) {
            System.out.println("Matrix " + matrixName + " is cached.");
            return;
        }
        if (matrixBrain.getExtractedMatrixMap().containsKey(matrixNames[0]) && matrixBrain.getExtractedMatrixMap().containsKey(matrixNames[1])) {
            try {
                //sync zato sto ceka mapu
                Matrix a = matrixBrain.getExtractedMatrixMap().get(matrixNames[0]).get();
                Matrix b = matrixBrain.getExtractedMatrixMap().get(matrixNames[1]).get();
                this.taskQueue.add(new MultiplyTask(a, b));

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void multiplyASync(String param) {
        String[] matrixNames = param.split(" ");
        if (matrixNames.length != 3) {
            System.out.println("Matrix params are wrong or flag is missing");
            return;
        }
        String matrixName = (matrixNames[0] + matrixNames[1]).toUpperCase();
        Map<String, Matrix> cache = matrixBrain.getCacheMap();

        if (cache.containsKey(matrixName)) {
            System.out.println("Matrix " + matrixName + " is cached.");
            return;
        }
        if (matrixBrain.getExtractedMatrixMap().containsKey(matrixNames[0]) && matrixBrain.getExtractedMatrixMap().containsKey(matrixNames[1])) {
            try {
                Matrix a = matrixBrain.getExtractedMatrixMap().get(matrixNames[0]).get();
                Matrix b = matrixBrain.getExtractedMatrixMap().get(matrixNames[1]).get();
                this.matrixBrain.multiplyAsync(this.taskQueue,a,b);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void dir(String param) {
        if (START_DIR!=null)
            this.systemExplorer.getRoots().add(START_DIR + param);
    }

    public void clear(String param) {
        matrixBrain.submitTask(param.trim(), "clear");
    }

    public void save(String param) {
        if (!param.contains("-name") || !param.contains("-file")) {
            System.out.println("Save parameters are bad.");
            return;
        }
        String[] split = param.split(" ");
        String matrixName = split[1];
        String fileName = split[3];

        matrixBrain.submitTask(matrixName + " " + fileName, "save");
    }

    public void poison() {
        System.out.println("stopping...");
        try {
            taskQueue.add(new PoisonTask());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.matrixBrain.getExecutorService().shutdownNow();
        this.systemExplorer.poison();

    }

    private void initComponents() {
        Thread explorerThread = new Thread(systemExplorer);
        explorerThread.start();
        this.taskCoordinator = new TaskCoordinator(taskQueue, matrixBrain);
        taskCoordinator.start();
    }


    private String parseParams(String[] params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i == 0) continue;

            sb.append(params[i]);
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
