package org.example.tasks;

import org.example.cli.MainCLI;
import org.example.model.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

public class CreateTask extends RecursiveTask<Matrix> implements Task {
    private String filePath;
    private int start;
    private int end;
    private Matrix matrixx;
    private File matrixFile;

    public CreateTask(File matrixFile) {
        this.matrixFile = matrixFile;
        this.start = 1;
        this.end = getNumOfLines(matrixFile.toPath());
        init();
    }

    private CreateTask(int start, int end, Matrix matrixx) {
        this.start = start;
        this.end = end;
        this.matrixx = matrixx;
    }

    private void init() {
        try (BufferedReader reader = new BufferedReader(new FileReader(matrixFile.getAbsolutePath()))) {
            String firstLine = reader.readLine();
            String[] matrixInfo = firstLine.split(", ");
            String matrixName = matrixInfo[0].split("=")[1];
            int rows = Integer.parseInt(matrixInfo[1].split("=")[1]);
            int cols = Integer.parseInt(matrixInfo[2].split("=")[1]);

            matrixx = new Matrix(matrixName, rows, cols);
            matrixx.setMatrixFile(matrixFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillMatrix(int start, int end) {
        try (BufferedReader reader = new BufferedReader(new FileReader(matrixx.getMatrixFile().getAbsolutePath()))) {
            for (int i = 0; i < start; i++)
                reader.readLine();
            for (int i = 0; i < end - start + 1; i++) {
                String line = reader.readLine();

                if (line == null)
                    break;

                String[] cellInfo = line.split(" = ");
                String[] index = cellInfo[0].split(",");
                int row = Integer.parseInt(index[0]);
                int col = Integer.parseInt(index[1]);

                matrixx.setValue(row, col, new BigInteger(cellInfo[1].trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Matrix compute() {
        if (end - start <= MainCLI.CHUNK_SIZE/30) {
            fillMatrix(start, end);
        } else {
            int mid = + start + ((end - start) / 2) ;
            CreateTask left = new CreateTask(start, mid, matrixx);
            CreateTask right = new CreateTask(mid, end, matrixx);

            left.fork();
            right.compute();
            left.join();
        }

        return matrixx;
    }
    private int getNumOfLines(Path path){
        long numOfLines = 0;
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            numOfLines = stream.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Math.toIntExact(numOfLines);

    }
    @Override
    public TaskType getType() {
        return TaskType.CREATE;
    }

    public File getMatrixFile() {
        return matrixFile;
    }


}
