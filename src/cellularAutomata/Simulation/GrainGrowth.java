package cellularAutomata.Simulation;

import cellularAutomata.Model.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class GrainGrowth extends Simulation{

    public boolean run = true;
    private ExecutorService executorService;


    public GrainGrowth(Grid grid) {
        super(grid);
//        this.executorService = Executors.newFixedThreadPool(12);
        this.executorService = Executors.newFixedThreadPool(12);

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
//        int x = random.nextInt(grid.getWidth());
//        int y = random.nextInt(grid.getHeight());
        int x = random.nextInt(grid.getHeight());
        int y = random.nextInt(grid.getWidth());
        int z = random.nextInt(grid.getDepth());

        if (grid.cellsList[x][y][z].idGrain == 0) {
            int id = grid.grainsList.size();
            grid.cellsList[x][y][z].idGrain = id;
            grid.cellsList[x][y][z].cellState = CellState.active;
            grid.grainsList.put(id, new Grain(id, GrainType.austenite, x, y, z, grid.carbon));
//            do fca
            grid.cellsListFCA.add(grid.cellsList[x][y][z]);
//            System.out.println("ziarno: " +  id + ", x,y,z: " +x+ "," + y+ ","+z);
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

                    if (grid.cellsList[i][j][k].cellState != CellState.active) {
                        this.run = true;

                        int newId = findNewId(i, j, k);

                        if (newId != 0) {
                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);
                        }

                        if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                            if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.iterationSimulation) {
                                grid.nextCellsList[i][j][k].cellState = CellState.active;
                                grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).addCell(grid.carbon); //zliczanie komórek w ziarnie
                            }
                        }
                    }
                }
            }
        }

        grid.cellsList = grid.nextCellsList;


        grid.iterationSimulation++;

        if(!this.run) {
            addCellState();
            grid.iterationSimulation = 0;
        }
    }

    public void growParallel(int numberOfThreads, ParallelDecomposition decomposition){
        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];

        this.run = false;

        int threats = numberOfThreads;
        List<Runnable> tasks = new ArrayList<>(threats);

        for (int task = 0; task < threats; task++) {
            int n = task;
            tasks.add(() -> {
                decomposition.decomposeAndExecute(n, threats, grid, this);
            });
        }

        try {
            executorService.invokeAll(tasks.stream().map(Executors::callable).collect(Collectors.toList()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        grid.cellsList = grid.nextCellsList;

        grid.iterationSimulation++;

        if(!this.run) {
            addCellState();
            grid.iterationSimulation = 0;
        }
    }

//    public void growParallel(int numberOfThreads){
//        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];
//
//        this.run = false;
//
//        int threats = numberOfThreads;
//        List<Runnable> tasks = new ArrayList<>(threats);
//
//        for (int task = 0; task < threats; task++) {
//            int n = task;
//            tasks.add(() -> {
//                for (int i = n * grid.getHeight() / threats; i < (n + 1) * grid.getHeight() / threats; i++) {
////                for (int i = 0; i < grid.getHeight(); i++) {
//                    for (int j = 0; j < grid.getWidth(); j++) {
//
//                        for (int k = 0; k < grid.getDepth(); k++) {
//
//                            grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);
//
//                            if (grid.cellsList[i][j][k].cellState != CellState.active) {
//                                this.run = true;
//
//                                int newId = findNewId(i, j, k);
//
//                                if (newId != 0) {
//                                    grid.nextCellsList[i][j][k].cellState = CellState.pending;
//                                    grid.nextCellsList[i][j][k].idGrain = newId;
//                                    grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);
//                                }
//
//                                if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {
//
//                                    if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.iterationSimulation) {
//                                        grid.nextCellsList[i][j][k].cellState = CellState.active;
//                                        grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).addCell(grid.carbon); //zliczanie komórek w ziarnie
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            });
//        }
//
//        try {
//            executorService.invokeAll(tasks.stream().map(Executors::callable).collect(Collectors.toList()));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        grid.cellsList = grid.nextCellsList;
//
//        if(!this.run) {
//            addCellState();
//            grid.iterationSimulation = 0;
//        }
//
//        grid.iterationSimulation++;
//    }

    public void dodajSasiadowDoListy(Cell cell){
        //        sąsiedztwo moore'a
        Map<Integer, Double> neighbours = new HashMap<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {

//                warunki brzegowe periodyczne
                    int X = (grid.getHeight() + cell.getX() + i) % grid.getHeight();
                    int Y = (grid.getWidth() + cell.getY() + j) % grid.getWidth();
                    int Z = (grid.getDepth() + cell.getZ() + k) % grid.getDepth();

//                    Jesli komórka w sąsiedztwie jest w stanie początkowym to zmieniamy jej stan na pośredni i dodajemy do listy komórek do następnego kroku czasowego
//                    if (grid.cellsList[X][Y][Z].cellState == CellState.notAlive) {
//                        grid.nextCellsList[X][Y][Z].cellState = CellState.transitional;
//                        grid.nextCellsListFCA.add(grid.cellsList[X][Y][Z]);
//                    }
//                    komórka zmienia stan na pending, dostaje id ziarna analizowanego i dodana jest do listy następnego kroku
                    if (grid.cellsList[X][Y][Z].cellState == CellState.notAlive) {
                        grid.nextCellsList[X][Y][Z].cellState = CellState.pending;
                        grid.nextCellsList[X][Y][Z].idGrain = cell.idGrain;
                        grid.nextCellsList[X][Y][Z].time = countDistance(grid.nextCellsList[X][Y][Z], X, Y, Z); //czy to ok?
                        grid.nextCellsListFCA.add(grid.cellsList[X][Y][Z]);
                    }
                }
            }
        }
    }

    public void growFCA(){
//        System.out.println("Numer iteracji w grain growFCA: " + grid.iterationSimulation);
//        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];
        grid.nextCellsList = grid.cellsList;

        grid.nextCellsListFCA = new ArrayList<>();
        this.run = false;

        for(Cell cell : grid.cellsListFCA) {
            this.run = true;

            if(grid.iterationSimulation == 0) {
                dodajSasiadowDoListy(cell);
            }

            else if (cell.cellState == CellState.pending) {
                if (((int) Math.ceil(cell.time)) <= grid.iterationSimulation) {
                    grid.nextCellsList[cell.getX()][cell.getY()][cell.getZ()].cellState = CellState.active;
                    grid.grainsList.get(cell.idGrain).addCell(grid.carbon); //zliczanie komórek w ziarnie
                    dodajSasiadowDoListy(cell);
                }
                else {
                    grid.nextCellsListFCA.add(cell);
                }
            }
        }

        grid.cellsList = grid.nextCellsList;
        grid.cellsListFCA = grid.nextCellsListFCA;

        grid.iterationSimulation++;

        if(!this.run) {
            addCellState();
            grid.iterationSimulation = 0;
        }
    }

//    @Override
//    public void grow() {
//        grid.nextCellsList = new Cell[grid.getHeight()][grid.getWidth()][grid.getDepth()];
//        this.run = false;
//
//        //        12 wątków
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        CountDownLatch latch = new CountDownLatch(grid.getHeight() * grid.getWidth() * grid.getDepth());
//
//        for (int i = 0; i < grid.getHeight(); i++) {
//            for (int j = 0; j < grid.getWidth(); j++) {
//                for (int k = 0; k < grid.getDepth(); k++) {
//                    final int x = i;
//                    final int y = j;
//                    final int z = k;
//
//                    executorService.submit(() -> {
//                        grid.nextCellsList[x][y][z] = new Cell(grid.cellsList[x][y][z]);
//
//                        if (grid.cellsList[x][y][z].cellState != CellState.active) {
//                            this.run = true;
//                            int newId = findNewId(x, y, z);
//
//                            if (newId != 0) {
//                                grid.nextCellsList[x][y][z].cellState = CellState.pending;
//                                grid.nextCellsList[x][y][z].idGrain = newId;
//                                grid.nextCellsList[x][y][z].time = countDistance(grid.nextCellsList[x][y][z], x, y, z);
//                            }
//
//                            if (grid.nextCellsList[x][y][z].cellState == CellState.pending) {
//                                if (((int) Math.ceil(grid.nextCellsList[x][y][z].time)) <= grid.iterationSimulation) {
//                                    grid.nextCellsList[x][y][z].cellState = CellState.active;
//                                    grid.grainsList.get(grid.nextCellsList[x][y][z].idGrain).addCell(grid.carbon); //zliczanie komórek w ziarnie
//                                }
//                            }
//                        }
//
//                        latch.countDown();
//                    });
//                }
//            }
//        }
//
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        executorService.shutdown();
//
//        grid.cellsList = grid.nextCellsList;
//
//        if (!this.run) {
//            addCellState();
//            grid.iterationSimulation = 0;
//        }
//
//        grid.iterationSimulation++;
//    }

    @Override
    public int findNewId(int x, int y, int z) {
//        sąsiedztwo moore'a
        Map<Integer, Double> neighbours = new HashMap<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {

//                warunki brzegowe periodyczne
                    int X = (grid.getHeight() + x + i) % grid.getHeight();
                    int Y = (grid.getWidth() + y + j) % grid.getWidth();
                    int Z = (grid.getDepth() + z + k) % grid.getDepth();

                    if (grid.cellsList[X][Y][Z].cellState == CellState.active) {
                        neighbours.putIfAbsent(grid.cellsList[X][Y][Z].idGrain, countDistance(grid.cellsList[X][Y][Z], x, y, z));
                    }
                }
            }
        }
        var optMinGrain = neighbours.entrySet().stream().min((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1);
        return optMinGrain.map(Map.Entry::getKey).orElse(0);
    }

    public void addCellState() {
        grid.cellsListOnTheBorder = new ArrayList<>();

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    if (grid.cellsList[i][j][k].getGrainType(grid) == GrainType.austenite) {
                        Set<Integer> neighbours = new HashSet<>();

                        for (int m = -1; m <= 1; m++) {
                            for (int n = -1; n <= 1; n++) {
                                for (int o = -1; o <= 1; o++) {

//                        do periodycznych warunków
                                    int X = (grid.getHeight() + i + m) % grid.getHeight();
                                    int Y = (grid.getWidth() + j + n) % grid.getWidth();
                                    int Z = (grid.getDepth() + k + o) % grid.getDepth();

                                    if (grid.cellsList[X][Y][Z].getGrainType(grid) == GrainType.austenite)
                                        neighbours.add(grid.cellsList[X][Y][Z].idGrain);
                                }
                            }

                            if (neighbours.size() > 1) {
                                grid.cellsListOnTheBorder.add(grid.cellsList[i][j][k]);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isRun() {
        return run;
    }
}
