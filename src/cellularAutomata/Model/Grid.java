package cellularAutomata.Model;

import Utils.Observer;
import cellularAutomata.Simulation.AusteniteFerriteTransformation;
import cellularAutomata.Simulation.GrainGrowth;
import javafx.animation.AnimationTimer;

import java.util.*;

public class Grid {
    private int height;
    private int width;
    private int depth;
    private int numberOfGrainsAustenite;

    public int iterationSimulation;
    public Cell[][][] cellsList;
    public Cell[][][] nextCellsList;
    public List<Cell> cellsListOnTheBorder;
    public List<Cell> allCellsOnTheBorder;

    public Map<Integer, Grain> grainsList;
    private Random random = new Random();

    private GrainGrowth grainGrowth;
    private AusteniteFerriteTransformation austeniteFerriteTransformation;

    private List<Observer> observersList;

    private AnimationTimer animationTimer;
    private int iterations;

    public double carbon = 0.5;

    public Grid(int height, int width, int depth, int numberOfGrainsAustenite) {

        this.height = height;
        this.width = width;
        this.depth = depth;
        this.numberOfGrainsAustenite = numberOfGrainsAustenite;

        this.cellsList = new Cell[this.height][this.width][this.depth];
        initGrid();

        this.grainsList = new HashMap<>();
        this.grainsList.put(0, new Grain(0, GrainType.austenite, 0, 0, 0, 0));

        this.grainGrowth = new GrainGrowth(this);
        this.austeniteFerriteTransformation = new AusteniteFerriteTransformation(this);

        this.observersList = new ArrayList<>();

        this.iterationSimulation = 0;
    }

//    public Grid(int height, int width, int depth, List<List<Integer>> simulationState) {
//
//        this.height = height;
//        this.width = width;
//        this.depth = depth;
//
//        this.grainsList = new HashMap<>();
//        this.grainsList.put(0, new Grain(0, GrainType.austenite, 0, 0, 0, 0));
//
////        do sprawdzenia
//        for(var row: simulationState) {
//            for(var cell: row) {
//                grainsList.putIfAbsent(cell, new Grain(cell, GrainType.austenite, 0, 0, 0, 0.5));
//                grainsList.get(cell).addCell(0.5);
//            }
//        }
//
//        this.cellsList = new Cell[this.height][this.width][this.depth];
//        this.nextCellsList = new Cell[this.height][this.width][this.depth];
//
//        // tutaj zmodyfikuj dla 3 wymiarów
//        int i = 0, j, k;
//        for(var row: simulationState) {
//            j = 0;
//            for(var cell: row) {
//                cellsList[i][j] = new Cell(i, j, cell);
//                j++;
//            }
//            i++;
//        }
//
//        this.grainGrowth = new GrainGrowth(this);
//
//        this.observersList = new ArrayList<>();
//
//        this.iterationSimulation = 0;
//
//        this.austeniteFerriteTransformation = new AusteniteFerriteTransformation(this);
//        this.austenitePearliteTransformation = new AustenitePearliteTransformation(this);
//
//    }

    public void notifyObservers() {
        for (Observer observer : this.observersList) {
            observer.onValueChanged();
        }
    }

    public void addObserver(Observer observer) {
        this.observersList.add(observer);
    }

    private void initGrid() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    this.cellsList[i][j][k] = new Cell(i, j, k);
                }
            }
        }
    }

    public void countBorderStateAustenit(){
        grainGrowth.addCellState();
    }

    public boolean grainGrowthRun(){
        return grainGrowth.isRun();
    }

    public boolean austeniteFerriteRun(){
        return austeniteFerriteTransformation.isRun();
    }

    public void grainGrowthSimulationInit() {
        grainGrowth.initGrains();
        notifyObservers();
    }

    public void grainGrowthSimulationRunParallel(int numberOfThreads){
        grainGrowth.growParallel(numberOfThreads);
    }

    public void grainGrowSimulationRun(){
        grainGrowth.grow();
//        notifyObservers();
    }

    public void grainGrowthOneStep() {
        grainGrowth.grow();
        notifyObservers();
    }

    public void austeniteFerriteSimulationRun(){
        austeniteFerriteTransformation.grow();
//        notifyObservers();

    }

    public void austeniteFerriteInit(){
        austeniteFerriteTransformation.initGrains();
        notifyObservers();
    }

//    public void austenitePearliteSimulationRun(){
//        austenitePearliteTransformation.grow();
//        notifyObservers();
//
//    }
//
//    public void austenitePearliteInit(){
//        austenitePearliteTransformation.initGrains();
//        notifyObservers();
//    }

    public void correctCarbon(double carbon){
        this.carbon = carbon;
        for (int i = 1; i < this.grainsList.size(); i++) {
            this.grainsList.get(i).changeInitAustenitCarbon(carbon);
        }
    }

    public void countBorderForAll() {
        this.allCellsOnTheBorder = new ArrayList<>();

        boolean toAdd = false;

        for (int i = 0; i < this.getHeight(); i++) {
            for (int j = 0; j < this.getWidth(); j++) {
                for(int k=0; k< this.getDepth(); k++) {
                    toAdd = false;
                    int currentGrainId = this.cellsList[i][j][k].idGrain;

//                    sąsiedztwo moorea z zasięgiem 1
                    for (int m = -1; m <= 1; m++) {
                        for (int n = -1; n <= 1; n++) {
                            for (int o = -1; o <= 1; o++) {
//                        do periodycznych warunków
                                int X = (this.getHeight() + i + m) % this.getHeight();
                                int Y = (this.getWidth() + j + n) % this.getWidth();
                                int Z = (this.getDepth() + k + o) % this.getDepth();

                                if (this.cellsList[X][Y][Z].idGrain != currentGrainId)
                                    toAdd = true;

                            }
                        }
                    }
                    if (toAdd)
                        this.allCellsOnTheBorder.add(this.cellsList[i][j][k]);
                }
            }
        }
    }

//    public void countBorderStateAustenitePearlite(){
//        austeniteFerriteTransformation.addCellState();
//    }

    public GrainType getGrainType(int id){
        return grainsList.get(id).getGrainType();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public int getNumberOfGrainsAustenite() {
        return numberOfGrainsAustenite;
    }
}
