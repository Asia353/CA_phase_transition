package cellularAutomata.Model;

import Utils.Observer;
import cellularAutomata.Simulation.*;
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
    public List<Cell> cellsListFCA;
    public List<Cell> nextCellsListFCA;

    public Map<Integer, Grain> grainsList;
    private Random random = new Random();

    private GrainGrowth grainGrowth;
    private AusteniteFerriteTransformation austeniteFerriteTransformation;

    private List<Observer> observersList;

    private AnimationTimer animationTimer;
    private int iterations;

    public double carbon = 0.5;
    public int parallelDecompositionType = 1; // row, 2 - column

//    public ParallelDecomposition parallelDecomposition;

    public Grid(int height, int width, int depth, int numberOfGrainsAustenite) {

        this.height = height;
        this.width = width;
        this.depth = depth;
        this.numberOfGrainsAustenite = numberOfGrainsAustenite;

        this.cellsList = new Cell[this.height][this.width][this.depth];
        this.cellsListFCA = new ArrayList<>();
        initGrid();

        this.grainsList = new HashMap<>();
        this.grainsList.put(0, new Grain(0, GrainType.austenite, 0, 0, 0, 0));

        this.grainGrowth = new GrainGrowth(this);
        this.austeniteFerriteTransformation = new AusteniteFerriteTransformation(this);

        this.observersList = new ArrayList<>();

        this.iterationSimulation = 0;
    }

    public Grid(int height, int width, int depth, int[][][] simulationState) {

        this.height = height;
        this.width = width;
        this.depth = depth;

        this.grainsList = new HashMap<>();
        this.grainsList.put(0, new Grain(0, GrainType.austenite, 0, 0, 0, 0));

        this.cellsList = new Cell[this.height][this.width][this.depth];
        this.cellsListFCA = new ArrayList<>();

        this.nextCellsList = new Cell[this.height][this.width][this.depth];

        // Tutaj zmodyfikuj dla 3 wymiarów
        for (int k = 0; k < this.depth; k++) {
            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < this.width; j++) {
                    int cellID = simulationState[i][j][k];
                    grainsList.putIfAbsent(cellID, new Grain(cellID, GrainType.austenite, i, j, k, this.carbon));
                    if (cellID == 0) {
                        this.cellsList[i][j][k] = new Cell(i,j,k);
//                        grainsList.get(cellID).addCell(this.carbon);
                    }
                    else {
                        grainsList.get(cellID).addCell(this.carbon);
                        cellsList[i][j][k] = new Cell(i, j, k, cellID);
                        this.cellsListFCA.add(this.cellsList[i][j][k]);
                    }
                }
            }
        }

        this.observersList = new ArrayList<>();

        this.iterationSimulation = 0;

        this.grainGrowth = new GrainGrowth(this);
        this.austeniteFerriteTransformation = new AusteniteFerriteTransformation(this);
    }

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

    public void grainGrowSimulationRun(){
        grainGrowth.grow();
//        notifyObservers();
    }

    public void grainGrowthSimulationRunParallel(int numberOfThreads){
        if(this.parallelDecompositionType == 1) {
            grainGrowth.growParallel(numberOfThreads, new RowDecomposition());
        }
        else if(this.parallelDecompositionType == 2){
            grainGrowth.growParallel(numberOfThreads, new ColumnDecomposition());
        }
    }

    public void grainGrowthSimulationRunFCA(){
        grainGrowth.growFCA();
    }

    public void grainGrowthOneStep() {
        grainGrowth.grow();
        notifyObservers();
    }

    public void austeniteFerriteSimulationRun(){
        austeniteFerriteTransformation.grow();
//        notifyObservers();
    }

    public void austeniteFerriteSimulationRunParallel(int numberOfThreads){
        austeniteFerriteTransformation.growParallel(numberOfThreads);
    }

    public void austeniteFerriteSimulationRunFCA(){
        austeniteFerriteTransformation.growFCA();
//        notifyObservers();
    }

    public void austeniteFerriteInit(){
        austeniteFerriteTransformation.initGrains();
        notifyObservers();
    }

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
