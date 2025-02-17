package cellularAutomata.Simulation;

import cellularAutomata.Model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class GrainGrowth extends Simulation{

    public boolean run = true;
    private ExecutorService executorService;
    public NeighborhoodSearchPattern neighborhoodSearchPattern;

    public GrainGrowth(Grid grid) {
        super(grid);
        this.executorService = Executors.newFixedThreadPool(12);

//        zmiana sposobu sprawdzania sąsiadów
        this. neighborhoodSearchPattern = new NeighborFirstGrain(grid, this); // II drugi wariant
//        this. neighborhoodSearchPattern = new NeighborDominantGrain(grid, this); // I wariant
    }

    public void setThreadsNumber(int n){
        this.executorService =  Executors.newFixedThreadPool(n);
    }

    @Override
    public void initGrains(){
        for (int i = 0; i < grid.getNumberOfGrainsAustenite();) {
            if (addRandomNewGrain()) i++;
        }

//        do sprawdzenia
//        for (int i =0; i<grid.cellsListFCA.size();i++) {
//            System.out.println(i + ": " + grid.cellsListFCA.get(i).idGrain);
//        }
    }

    private boolean addRandomNewGrain(){
        Random random = new Random();
        int x = random.nextInt(grid.getHeight());
        int y = random.nextInt(grid.getWidth());
        int z = random.nextInt(grid.getDepth());

        if (grid.cellsList[x][y][z].idGrain == 0) {
            int id = grid.grainsList.size();
            grid.cellsList[x][y][z].idGrain = id;
            grid.cellsList[x][y][z].cellState = CellState.alive;
            grid.grainsList.put(id, new Grain(id, GrainType.austenite, x, y, z, grid.carbon));
//            do fca
            grid.cellsListFCA.add(grid.cellsList[x][y][z]);
            return true;
        }
        return false;
    }

    @Override
    public void grow(){
        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];;

        this.run = false;

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    if (grid.cellsList[i][j][k].cellState != CellState.alive) {
                        this.run = true;

                        int newId = this.findNewId(i, j, k);

                        if (newId != 0) {
                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);
                        }

                        if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                            if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.iterationSimulation) {
                                grid.nextCellsList[i][j][k].cellState = CellState.alive;
                                grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).addCell(grid.carbon);
                            }
                        }
                    }
                }
            }
        }

        grid.cellsList = grid.nextCellsList;


        grid.iterationSimulation++;

        if(!this.run) {
            grid.iterationSimulation = 0;
        }
    }

    public void growParallel(int numberOfThreads, ParallelDecomposition decomposition){
        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];

        this.run = false;

        int threats = numberOfThreads;
        List<Runnable> tasks = new ArrayList<>(threats);

        synchronized (grid.grainsList) {
            for (int task = 0; task < threats; task++) {
                int n = task;
                tasks.add(() -> {
                    decomposition.decomposeAndExecute(n, threats, grid, this);
                });
            }
        }

        try {
            executorService.invokeAll(tasks.stream().map(Executors::callable).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        grid.cellsList = grid.nextCellsList;

        grid.iterationSimulation++;

        if(!this.run) {
            grid.iterationSimulation = 0;
        }
    }

    public void addNeighborsToTheFrontalList(Cell cell){
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {

                    int X = (grid.getHeight() + cell.getX() + i) % grid.getHeight();
                    int Y = (grid.getWidth() + cell.getY() + j) % grid.getWidth();
                    int Z = (grid.getDepth() + cell.getZ() + k) % grid.getDepth();

//                    komórka zmienia stan na pending, dostaje id ziarna analizowanego i dodana jest do listy następnego kroku
                    if (grid.cellsList[X][Y][Z].cellState == CellState.notAlive) {
                        grid.cellsList[X][Y][Z].cellState = CellState.pending;
                        grid.cellsList[X][Y][Z].idGrain = cell.idGrain;
                        grid.cellsList[X][Y][Z].time = countDistance(grid.cellsList[X][Y][Z], X, Y, Z); //czy to ok?
                        grid.nextCellsListFCA.add(grid.cellsList[X][Y][Z]);
                    }
                }
            }
        }
    }

    public void growFCA(){
//        System.out.println("Numer iteracji w grain growFCA: " + grid.iterationSimulation);
//        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];
//        grid.nextCellsList = grid.cellsList;

        grid.nextCellsListFCA = new LinkedList<>();
        this.run = false;

        for(Cell cell : grid.cellsListFCA) {
            this.run = true;

            if(grid.iterationSimulation == 0) {
                addNeighborsToTheFrontalList(cell);
            }

            else if (cell.cellState == CellState.pending) {
                if (((int) Math.ceil(cell.time)) <= grid.iterationSimulation) {
                    grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].cellState = CellState.alive;
                    grid.grainsList.get(cell.idGrain).addCell(grid.carbon); //zliczanie komórek w ziarnie
                    addNeighborsToTheFrontalList(cell);
                }
                else {
                    grid.nextCellsListFCA.add(cell);
                }
            }
        }

//        grid.cellsList = grid.nextCellsList;
        grid.cellsListFCA = grid.nextCellsListFCA;

        grid.iterationSimulation++;

        if(!this.run) {
            grid.iterationSimulation = 0;
        }
    }

    @Override
    public int findNewId(int x, int y, int z) {
        return this.neighborhoodSearchPattern.findNewId(x, y, z);
    }

    public void addCellState() {
        grid.cellsListOnTheBorder = new ArrayList<>();

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    Set<Integer> neighbours = new HashSet<>();

                    for (int m = -1; m <= 1; m++) {
                        for (int n = -1; n <= 1; n++) {
                            for (int o = -1; o <= 1; o++) {

                                int X = (grid.getHeight() + i + m) % grid.getHeight();
                                int Y = (grid.getWidth() + j + n) % grid.getWidth();
                                int Z = (grid.getDepth() + k + o) % grid.getDepth();

                                neighbours.add(grid.cellsList[X][Y][Z].idGrain);
                            }
                        }
                    }
                    if (neighbours.size() > 1) {
                        grid.cellsListOnTheBorder.add(grid.cellsList[i][j][k]);
                    }
                }
            }
        }
        System.out.println("Liczba komórek na granicy: " + grid.cellsListOnTheBorder.size());
    }

    public boolean isRun() {
        return run;
    }
}
