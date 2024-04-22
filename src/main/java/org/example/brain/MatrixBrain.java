package org.example.brain;

import org.example.cli.MainCLI;
import org.example.model.Matrix;
import org.example.queue.TaskQueue;
import org.example.tasks.BrainTask;
import org.example.tasks.CreateTask;
import org.example.tasks.MultiplyTask;
import org.example.tasks.Task;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

public class MatrixBrain {
    private final Map<String, Future<Matrix>> extractedMatrixMap = new ConcurrentHashMap<>();
    private final Map<String, Matrix> cacheMap = new ConcurrentHashMap<>();
    private final Map<String, Matrix> allMatrixMap = new ConcurrentHashMap<>();
    private final Map<String, Matrix> multiplyMap = new ConcurrentHashMap<>();
    private ExecutorService executorService;

    public MatrixBrain() {
        this.executorService = Executors.newCachedThreadPool();
    }

    public void submitTask(String query, String command) {
        this.executorService.submit(new BrainTask(this, query, command));
    }

    public void clear(String matrixName) {
        if (!allMatrixMap.containsKey(matrixName)) {
            System.out.println("The matrix " + matrixName + " doesn't exist in the system, clear failed.");
            return;
        }
        multiplyMap.forEach((key, value) -> {
            String[] split = key.split(",");
            if (split[0].equals(matrixName) || split[1].equals(matrixName)) {
                multiplyMap.remove(key);
                allMatrixMap.remove(split[0] + split[1]);
                cacheMap.remove(split[0] + split[1]);
                try {
                    Future<Matrix> fm = extractedMatrixMap.get(split[0]+split[1]);
                    Matrix mat = fm.get();
                    extractedMatrixMap.values().remove(fm);
                    System.out.println("cleared");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Matrix " + key + " succesfully removed");
            }
        });

        allMatrixMap.remove(matrixName);
        if(extractedMatrixMap.containsKey(matrixName))
            extractedMatrixMap.remove(matrixName);
        if(extractedMatrixMap.containsKey(matrixName))
            cacheMap.remove(matrixName);

        System.out.println("Successfully removed "+matrixName);

    }

    public void save(String matrixName, String fileName) {
        if (!allMatrixMap.containsKey(matrixName)) {
            System.out.println("Matrix " + matrixName + " is not in the system.");
            return;
        }
        if (allMatrixMap.get(matrixName) == null) {
            System.out.println("Matrix " + matrixName + " is null.");
            return;
        }

        Matrix matrix = allMatrixMap.get(matrixName);
        File matrixFile = new File(MainCLI.START_DIR + fileName);
        try {
            if (matrixFile.createNewFile()) {
                System.out.println("Created new file on the path" + matrixFile.getAbsolutePath());
            } else {
                System.out.println("File with this name " + fileName + " already exists.");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(matrixFile))) {
            writer.println("matrix_name=" + matrix.getName() + ", rows=" + matrix.getRows() + ", cols=" + matrix.getCols());
            for (int j = 0; j < matrix.getCols(); j++) {
                for (int i = 0; i < matrix.getRows(); i++) {
                    if (!matrix.getValue(i, j).equals(BigInteger.ZERO)) {
                        writer.println(i + "," + j + " = " + matrix.getValue(i, j));
                    }
                }
            }
            System.out.println("Matrix " + matrixName + " successfully saved, file name: " + matrixFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void infoS(int n) {
        List<Matrix> infoList = new ArrayList<>();
        List<Matrix> finalInfoList = new ArrayList<>();
        this.extractedMatrixMap.forEach((key, value) -> {
            try {
                finalInfoList.add(value.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        infoList.addAll(finalInfoList);
        infoList.sort(Comparator.comparing(Matrix::getName));
        infoList = infoList.subList(0, Math.min(infoList.size(), n));
        StringBuilder sb = new StringBuilder();
        for (Matrix m : infoList) {
            sb.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        System.out.println(sb);
    }

    public void infoE(int n) {
        List<Matrix> infoList = new ArrayList<>();
        List<Matrix> finalInfoList = new ArrayList<>();
        this.extractedMatrixMap.forEach((key, value) -> {
            try {
                finalInfoList.add(value.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        infoList.addAll(finalInfoList);
        infoList.sort(Comparator.comparing(Matrix::getName).reversed());
        infoList = infoList.subList(0, Math.min(infoList.size(), n));
        StringBuilder sb = new StringBuilder();
        for (Matrix m : infoList) {
            sb.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        System.out.println(sb);
    }

    public void info(String query) {
        try {
            if (extractedMatrixMap.containsKey(query)) {
                if (extractedMatrixMap.get(query).isDone()) {
                    Matrix m = extractedMatrixMap.get(query).get();
                    System.out.println("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols());
                } else {
                    System.out.println("Matrix is not done processing.");
                }
            } else {
                System.out.println("Matrix " + query + " doesn't exist.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void infoAll() {
        try {
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, Future<Matrix>> entry : extractedMatrixMap.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue().isDone()) {
                    Future<Matrix> futureMatrix = entry.getValue();
                    Matrix m = futureMatrix.get();
                    sb.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
                }
            }
            if (!sb.isEmpty()) {
                System.out.println(sb);
//                Thread.currentThread().sleep(60000);
                return;
            }

            System.out.println("The map is empty");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void infoAsc() {
        try {
            List<Matrix> matrixList = new ArrayList<>();

            for (Map.Entry<String, Future<Matrix>> entry : extractedMatrixMap.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue().isDone()) {
                    Future<Matrix> futureMatrix = entry.getValue();
                    Matrix m = futureMatrix.get();
                    matrixList.add(m);
                }
            }
            if (!matrixList.isEmpty()) {
                System.out.println(sortMatrixAsc(matrixList));
                return;
            }

            System.out.println("The map is empty");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void infoDesc() {
        try {
            List<Matrix> matrixList = new ArrayList<>();

            for (Map.Entry<String, Future<Matrix>> entry : extractedMatrixMap.entrySet()) {
                String key = entry.getKey();
                if (entry.getValue().isDone()) {
                    Future<Matrix> futureMatrix = entry.getValue();
                    Matrix m = futureMatrix.get();
                    matrixList.add(m);
                }
            }
            if (!matrixList.isEmpty()) {
                System.out.println(sortMatrixDesc(matrixList));
                return;
            }

            System.out.println("The map is empty");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void multiply(String nameA, String nameB) {
        try {
            //System.out.println("Waiting for multiplication in brain");
            //Thread.sleep(60000);
            ///System.out.println("Brain waking up");
            Matrix m = extractedMatrixMap.get((nameA + nameB).toUpperCase()).get();

            addToCache(m);
            allMatrixMap.putIfAbsent(m.getName(), m);
            multiplyMap.putIfAbsent(nameA.toUpperCase() + "," + nameB.toUpperCase(), m);
            System.out.println("Matrix multiplication " + m.getName() + ", added to cache");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
    public String sortMatrixDesc(List<Matrix> matrixList) {
        StringBuilder builder = new StringBuilder();
        matrixList.sort(Comparator.comparing(Matrix::getRows).reversed());
        builder.append("Matrices sorted in descending order by rows\n");
        for (Matrix m : matrixList) {
            builder.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        builder.append("Matrices sorted in descending order by cols\n");
        matrixList.sort(Comparator.comparing(Matrix::getCols).reversed());
        for (Matrix m : matrixList) {
            builder.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        return builder.toString();
    }

    public String sortMatrixAsc(List<Matrix> matrixList) {
        StringBuilder builder = new StringBuilder();
        matrixList.sort(Comparator.comparing(Matrix::getRows));
        builder.append("Matrices sorted in ascending order by rows\n");
        for (Matrix m : matrixList) {
            builder.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        builder.append("Matrices sorted in ascending order by cols\n");
        matrixList.sort(Comparator.comparing(Matrix::getCols));
        for (Matrix m : matrixList) {
            builder.append("Name: " + m.getName() + " rows: " + m.getRows() + " cols: " + m.getCols() + "\n");
        }
        return builder.toString();
    }

    public void addCreateTask(CreateTask createTask, Future<Matrix> futureMatrix) {
        String fileName = new File(createTask.getMatrixFile().getAbsolutePath()).getName();
        int extensionDot = fileName.lastIndexOf(".");
        String name = fileName.substring(0, extensionDot);
        this.extractedMatrixMap.put(name, futureMatrix);

    }

    public void addMultiplyTask(Matrix a, Matrix b, Future<Matrix> matrixFuture) {
        //async -> block matrix brain
        this.extractedMatrixMap.putIfAbsent((a.getName() + b.getName()).toUpperCase(), matrixFuture);
        /*

        try {
            System.out.println("Waiting for multiplication in matrix brain");
            Matrix m = matrixFuture.get();
            //m.printMatrix();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        /*
        try {
            Matrix matrix  = matrixFuture.get();
            this.getAllMatrixMap().put(matrix.getName(), matrix);
            this.getCacheMap().put(matrix.getName(),matrix);
            System.out.println("Matrix " +matrix.getName() + "calculated in matrix brain");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

         */
    }

    public void multiplyAsync(TaskQueue taskQueue, Matrix matrixA, Matrix matrixB) {
        if (extractedMatrixMap.containsKey(matrixA.getName()) && extractedMatrixMap.containsKey(matrixB.getName()))
        {
            System.out.println("Matrix "+matrixA +" and " + matrixB +" are not in the system");
            return;
        }
        try {
            taskQueue.add(new MultiplyTask(matrixA,matrixB));
            System.out.println("Waiting for async multiplication " + matrixA.getName()+matrixB.getName());
            Matrix m = this.getExtractedMatrixMap().get((matrixA.getName()+matrixB.getName()).toUpperCase()).get();
            System.out.println("Multiplication async successful "+m.getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Future<Matrix>> getExtractedMatrixMap() {
        return extractedMatrixMap;
    }

    public void addToCache(Matrix m) {
        cacheMap.put(m.getName(), m);
    }

    public Matrix getMatrixFromMap(String name) {
        try {
            if (extractedMatrixMap.containsKey(name)) {
                Future<Matrix> matrixFuture = extractedMatrixMap.get(name);
                Matrix matrix = matrixFuture.get();
                return matrix;
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Map<String, Matrix> getAllMatrixMap() {
        return allMatrixMap;
    }

    public Map<String, Matrix> getCacheMap() {
        return cacheMap;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
