package org.example.tasks;

import org.example.brain.MatrixBrain;

public class BrainTask implements Runnable, Task {
    private MatrixBrain matrixBrain;
    private String query;
    private String command;

    public BrainTask(MatrixBrain matrixBrain, String query, String command) {
        this.matrixBrain = matrixBrain;
        this.query = query;
        this.command = command;
    }

    //062 105 8051
    @Override
    public void run() {
        switch (command) {
            case "info":
                if (!query.contains("-s") && !query.contains("-e")) {
                    switch (query) {
                        case "-all":
                            this.matrixBrain.infoAll();
                            break;
                        case "-desc":
                            this.matrixBrain.infoDesc();
                            break;
                        case "-asc":
                            this.matrixBrain.infoAsc();
                            break;
                        default:
                            this.matrixBrain.info(query);
                            break;
                    }
                } else {
                    String[] split = query.split(",");
                    if (split[0].equals("-s")) {
                        this.matrixBrain.infoS(Integer.valueOf(split[1]));
                        break;

                    } else if (split[0].equals("-e")) {
                        this.matrixBrain.infoE(Integer.valueOf(split[1]));
                        break;

                    } else System.out.println("Flag for start or end is not set.");
                }
            case "multiply":
                String[] matrices = query.split(" ");
                this.matrixBrain.multiply(matrices[0], matrices[1]);
                break;
            case "save":
                String[] split = query.trim().split(" ");
                this.matrixBrain.save(split[0], split[1]);
                break;
            case "clear":
                this.matrixBrain.clear(query);
                //this.matrixBrain.get
                break;

        }

        //System.out.println("Matrix " + query + " doesn't exist, command:" + command);
    }

    @Override
    public TaskType getType() {
        return TaskType.BRAIN;
    }


}
