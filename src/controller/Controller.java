package controller;

import cellularAutomata.Model.Grid;
import cellularAutomata.Visualization.Visualizer;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.*;
import java.util.*;

public class Controller {

    private AppController appController;

    @FXML
    private Pane paneVisualize;
    @FXML
    private Canvas canvas;
    @FXML
    private TextField textFieldC;
    @FXML
    private ComboBox<String> comboBoxOptions;
    @FXML
    private ComboBox<String> comboBoxParallel;

    private Grid grid;
    private Visualizer visualizer;

    private int height = 120;
    private int width = 120;
    private int depth = 120;
    private int numberOfGrains = 100;
    private int visScale = 1;
    private int numberOfThreads = 12;
//    0 - sekwencyjny, 1 - rónoległy, 2 - frontalny
    private int simulationType = 0;

    public void setAppController(AppController appController) {
        this.appController = appController;
//        this.canvas.setHeight((height+depth+depth+2) * visScale);
//        this.canvas.setWidth((width+width+depth+depth+3) * visScale);
        this.canvas.setHeight(850);
        this.canvas.setWidth(850);
    }

    public void handleBtnInit(ActionEvent event) {
        this.grid = new Grid(height, width, depth, numberOfGrains); // tutaj tworzę grid, vizualizację i sumulację
        this.visualizer = new Visualizer(this. grid, this.canvas, this.visScale);
        this.visualizer.canvasClear();
        this.grid.addObserver(visualizer);
        textFieldC.setText(String.valueOf(grid.carbon));

        long millisActualTime = System.currentTimeMillis(); // początkowy czas w milisekundach.

        grid.grainGrowthSimulationInit();

        long executionTime = (System.currentTimeMillis() - millisActualTime);
        System.out.println("Czas inicjalizacji rozrostu: " + executionTime * 0.001);

    }

    public void initialize() {
        comboBoxOptions.setOnAction(event -> {
            String selectedOption = comboBoxOptions.getValue();
//            System.out.println("Automat komórkowy sekwencyjny");
            if (selectedOption.equals("cellular automata")) {
                this.simulationType = 0;
            } else if (selectedOption.equals("parallel cellular automata")) {
//                System.out.println("Automat komórkowy równoległy");
                this.simulationType = 1;
            } else if (selectedOption.equals("frontal cellular automata")) {
//                System.out.println("Frontalny automat komórkowy");
                this.simulationType = 2;
            }
        });
    }

//    public void parallelDecomposition() {
//        comboBoxParallel.setOnAction(event -> {
//            String selectedOption = comboBoxParallel.getValue();
//            if (selectedOption.equals("row")) {
//                grid.parallelDecompositionType = 1;
//            } else if (selectedOption.equals("column")) {
//                grid.parallelDecompositionType = 2;
//            }
////            else if (selectedOption.equals("frontal cellular automata")) {
////                this.simulationType = 3;
////            }
//        });
//    }

    public void handleBtnOneStep(ActionEvent event) {
        grid.grainGrowthOneStep();
    }

    public void handleBtnRun(ActionEvent event) {
        long millisActualTime = System.currentTimeMillis(); // początkowy czas w milisekundach.

        if(this.simulationType == 0) {
            System.out.println("Automat komórkowy sekwencyjny");
            while (grid.grainGrowthRun()) {
                grid.grainGrowSimulationRun();
            }
        }
        else if(this.simulationType == 1) {
            System.out.println("Automat komórkowy równoległy (wątki: " + this.numberOfThreads + ", dekompozycja: " + grid.parallelDecompositionType +  " (1-row, 2-column, 3-cube))");
            while (grid.grainGrowthRun()) {
                grid.grainGrowthSimulationRunParallel(this.numberOfThreads);
            }
        }

        else{
            System.out.println("Frontalny automat komórkowy");
            while (grid.grainGrowthRun()) {
                grid.grainGrowthSimulationRunFCA();
            }
        }

        long executionTime = (System.currentTimeMillis() - millisActualTime);
        System.out.println("Czas rozrostu ziaren: " + executionTime * 0.001);
        grid.notifyObservers();

    }

    public void handleBtnBorder(ActionEvent event) {
        visualizer.printBorder();
    }

    public void TextFieldCHandle(ActionEvent event) {
        grid.correctCarbon(Double.parseDouble(this.textFieldC.getText()));
        textFieldC.setText(String.valueOf(grid.carbon));
    }

    public void handleBtnFerrite(ActionEvent event) {
        grid.iterationSimulation = 0;
        long millisActualTime = System.currentTimeMillis(); // początkowy czas w milisekundach.
        grid.austeniteFerriteInit();

        if(this.simulationType == 0) {
            System.out.println("Automat komórkowy sekwencyjny - austenit-ferryt");
            while(grid.austeniteFerriteRun()){
                grid.austeniteFerriteSimulationRun();
            }
        }
        else if(this.simulationType == 1) {
            System.out.println("Automat komórkowy równoległy - austenit-ferryt (wątki: " + this.numberOfThreads + ")");
            while (grid.austeniteFerriteRun()) {
                grid.austeniteFerriteSimulationRunParallel(this.numberOfThreads);
            }
        }

        else{
            System.out.println("Frontalny automat komórkowy - austenit-ferryt");
            while(grid.austeniteFerriteRun()){
                grid.austeniteFerriteSimulationRunFCA();
            }
        }

        long executionTime = (System.currentTimeMillis() - millisActualTime);
        System.out.println("Czas austenit-ferryt: " + executionTime * 0.001);

        grid.notifyObservers();
    }

    public void handleBtnSave(ActionEvent event) throws IOException {
        FileWriter write = new FileWriter("initMicrostructure.txt");

        write.write(String.valueOf(height) + "\n");// + System.lineSeparator());
        write.write(String.valueOf(width) + "\n");// + System.lineSeparator());
        write.write(String.valueOf(depth) + "\n");// + System.lineSeparator());

        for (int k = 0; k < depth; k++) {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    write.write(String.valueOf(grid.cellsList[i][j][k].idGrain + ","));
                }
                write.write("\n");
            }
//            write.write("\n");
        }
        write.close();
    }

    public void handleBtnLoad(ActionEvent event) throws IOException {
        Grid loadedGrid = readGrainsFromFile("initMicrostructure.txt");
        this.grid = loadedGrid;
        this.visualizer = new Visualizer(this.grid, this.canvas, this.visScale);
        this.visualizer.canvasClear();
        this.grid.addObserver(visualizer);
        this.grid.notifyObservers();
        textFieldC.setText(String.valueOf(grid.carbon));
        System.out.println("Mikrostruktura zaczytana z pliku \"initMicrostructure.txt\" ");
    }

    public static Grid readGrainsFromFile(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int height = Integer.parseInt(reader.readLine());
            int width = Integer.parseInt(reader.readLine());
            int depth = Integer.parseInt(reader.readLine());

            int[][][] loadedCellStates = new int[height][width][depth];

            for (int k = 0; k < depth; k++) {
                for (int i = 0; i < height; i++) {
                    String currentLine = reader.readLine();
                    String[] tokens = currentLine.split(",");
                    for (int j = 0; j < width; j++) {
                        loadedCellStates[i][j][k] = Integer.parseInt(tokens[j]);
                    }
                }
            }
            return new Grid(height, width, depth, loadedCellStates);
        }
    }

}
