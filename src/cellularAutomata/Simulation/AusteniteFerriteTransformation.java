package cellularAutomata.Simulation;

import cellularAutomata.Model.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AusteniteFerriteTransformation extends Simulation{
    public double ferrytCarbon = 0.0218;
    private double numberOfFerriteGrains;
    public boolean run = true;
    private ExecutorService executorService;


    public AusteniteFerriteTransformation(Grid grid) {
        super(grid);
        this.executorService = Executors.newFixedThreadPool(12);
    }

    @Override
    public void initGrains() {
        grid.cellsListFCA = new ArrayList<>();

        System.out.println("Sprawdzenie czy na pewno lista z zianrami pending jest pusta: " + grid.cellsListFCA.size());
        grid.countBorderStateAustenit();
        Collections.shuffle(grid.cellsListOnTheBorder);

//        numberOfFerriteGrains = grid.cellsListOnTheBorder.size() * 0.1; //domyślnie do 2D
        numberOfFerriteGrains = grid.cellsListOnTheBorder.size() * 0.01; //3D

//        System.out.println("number of ferrite: " + numberOfFerriteGrains );

        for (int i = 0; i < numberOfFerriteGrains; ) {

            if (grid.cellsListOnTheBorder.isEmpty()) break;

            Cell cell = grid.cellsListOnTheBorder.get(0);

            if(cell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(cell.idGrain).getCarbonConcentration() < 0.77) {
                int newId = grid.grainsList.size();
                int currentId = grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain;

                Grain grain = new Grain(newId, GrainType.ferrite, cell.getX(), cell.getY(), cell.getZ(), this.ferrytCarbon);
                grain.setParentGrain(currentId);
                grid.grainsList.put(newId, grain);

//                w deleteCell od razu modyfikowane jest stężenie węgla
                grid.grainsList.get(currentId).deleteCell(this.ferrytCarbon);
                grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain = newId;

                grid.cellsListFCA.add(grid.cellsList[cell.getX()][cell.getY()][cell.getZ()]);

                i++;
            }

            grid.cellsListOnTheBorder.remove(0);
        }
    }

    @Override
    public void grow(){

//        po co każde ziarno ma swoją iterację?
        for(int i = 0; i < grid.grainsList.size(); i++) {
            grid.grainsList.get(i).iteration++;
        }

        this.run = false;

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    int currentGrainID = grid.cellsList[i][j][k].idGrain;
                    Cell currentCell = grid.cellsList[i][j][k];

                    if (currentCell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(currentGrainID).getCarbonConcentration() < 0.77) {

                        int newId = findNewId(i, j, k);

                        if (newId != currentGrainID) {

                            this.run = true;

//                            currentCell?
                            grid.grainsList.get(grid.cellsList[i][j][k].idGrain).deleteCell(this.ferrytCarbon);
                            grid.grainsList.get(newId).addCell(this.ferrytCarbon);

                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);

                        }
                    }

                    if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                        this.run = true;

                        if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).iteration) {
                            grid.nextCellsList[i][j][k].cellState = CellState.active;
                        }
                    }
                }
            }
        }

        grid.cellsList = grid.nextCellsList;

        grid.iterationSimulation++;
//        System.out.println("iteracja symulacji: " + grid.iterationSimulation);
    }

    public void growParallel(int numberOfThreads, ParallelDecomposition decomposition) {

        for (int i = 0; i < grid.grainsList.size(); i++) {
            grid.grainsList.get(i).iteration++;
        }

        this.run = false;

        int threads = numberOfThreads;
        List<Runnable> tasks = new ArrayList<>(threads);

        synchronized (grid.grainsList) {
            for (int task = 0; task < threads; task++) {
                int n = task;
                tasks.add(() -> {
                    decomposition.decomposeAndExecuteFerrite(n, threads, grid, this);
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
//        if(run == false) {
//            System.out.println("iteracja a->f: " + grid.iterationSimulation);
//        }
//        System.out.println("iteracja symulacji: " + grid.iterationSimulation);
    }

    public void addNeighborsToTheFrontList(Cell cell){
        //        sąsiedztwo moore'a
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {

//                warunki brzegowe periodyczne
                    int X = (grid.getHeight() + cell.getX() + i) % grid.getHeight();
                    int Y = (grid.getWidth() + cell.getY() + j) % grid.getWidth();
                    int Z = (grid.getDepth() + cell.getZ() + k) % grid.getDepth();

//                    komórka zmienia stan na pending, dostaje id ziarna analizowanego i dodana jest do listy następnego kroku
                    if (grid.cellsList[X][Y][Z].getGrainType(grid) == GrainType.austenite & grid.grainsList.get(grid.cellsList[X][Y][Z].idGrain).getCarbonConcentration() < 0.77 & (grid.cellsList[X][Y][Z].idGrain == grid.grainsList.get(cell.idGrain).getParentGrain())) {

                        grid.grainsList.get(grid.cellsList[X][Y][Z].idGrain).deleteCell(this.ferrytCarbon);
                        grid.grainsList.get(cell.idGrain).addCell(this.ferrytCarbon);

                        grid.nextCellsList[X][Y][Z].cellState = CellState.pending;
                        grid.nextCellsList[X][Y][Z].idGrain = cell.idGrain;
                        grid.nextCellsList[X][Y][Z].time = countDistance(grid.nextCellsList[X][Y][Z], X, Y, Z);

                        grid.nextCellsListFCA.add(grid.cellsList[X][Y][Z]);
                    }
                }
            }
        }
    }

    public void growFCA(){

//        po co każde ziarno ma swoją iterację?
        for(int i = 0; i < grid.grainsList.size(); i++) {
            grid.grainsList.get(i).iteration++;
        }

        this.run = false;

        grid.nextCellsList = grid.cellsList; //?
        grid.nextCellsListFCA = new ArrayList<>();

//        System.out.println("numer iteracji ferryt: " + grid.iterationSimulation);

        for(Cell cell : grid.cellsListFCA) {

            this.run = true;

            if(grid.iterationSimulation == 0) {
                addNeighborsToTheFrontList(cell);
            }

            else if (cell.cellState == CellState.pending) {
                if (((int) Math.ceil(cell.time)) <= grid.iterationSimulation) {
                    grid.nextCellsList[cell.getX()][cell.getY()][cell.getZ()].cellState = CellState.active;
                    addNeighborsToTheFrontList(cell);
                }
                else {
                    grid.nextCellsListFCA.add(cell);
                }
            }
        }

        grid.cellsList = grid.nextCellsList;
        grid.cellsListFCA = grid.nextCellsListFCA;

        grid.iterationSimulation++;
    }

    @Override
    public int findNewId(int x, int y, int z) {
//        sąsiedztwo moore'a

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
//                warunki brzegowe periodyczne
                    int X = (grid.getHeight() + x + i) % grid.getHeight();
                    int Y = (grid.getWidth() + y + j) % grid.getWidth();
                    int Z = (grid.getDepth() + z + k) % grid.getDepth();

                    if ((grid.cellsList[x][y][z].idGrain == grid.grainsList.get(grid.cellsList[X][Y][Z].idGrain).getParentGrain()) && (grid.cellsList[X][Y][Z].cellState == CellState.active)) {
                        return grid.cellsList[X][Y][Z].idGrain;
                    }
                }
            }
        }
        return grid.cellsList[x][y][z].idGrain;
    }

    public boolean isRun() {
        return run;
    }

}
