package cellularAutomata.Simulation;

import cellularAutomata.Model.Cell;
import cellularAutomata.Model.CellState;
import cellularAutomata.Model.GrainType;
import cellularAutomata.Model.Grid;

public class CubeDecomposition implements ParallelDecomposition {

    @Override
    public void decomposeAndExecute(int task, int totalThreads, Grid grid, GrainGrowth grainGrowth) {
//        System.out.println("Dekompozycja blokowa rozrost");
        int w, h, d;

        if(totalThreads == 1) {
            h = 1;
            w = 1;
            d = 1;
        }

        else if(totalThreads == 2) {
            h = 2;
            w = 1;
            d = 1;
        }

        else if(totalThreads == 4) {
            h = 2;
            w = 2;
            d = 1;
        }

        else if(totalThreads == 8) {
            h = 2;
            w = 2;
            d = 2;
        }

        else if (totalThreads == 12) {
            h = 3;
            w = 2;
            d = 2;
        }
        else {
            h = grid.getHeight();
            w = grid.getWidth();
            d = grid.getDepth();

            System.out.println("błąd cube decompositon");
        }

        int cubeHeight = grid.getHeight() / h;
        int cubeWidth = grid.getWidth() / w;
        int cubeDepth = grid.getDepth() / d;

//        dla 12 wątków
//        int h_idx = (task % 6) / 2;
//        int w_idx = task % 2;
//        int d_idx = task / 6;

        int h_idx = (task % (totalThreads/d)) / w;
        int w_idx = task % w;
        int d_idx = task / (totalThreads/d);

        for (int i = h_idx * cubeHeight; i < (h_idx + 1) * cubeHeight; i++) {
            for (int j = w_idx * cubeWidth; j < (w_idx + 1) * cubeWidth; j++) {
                for (int k = d_idx * cubeDepth; k < (d_idx + 1) * cubeDepth; k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    if (grid.cellsList[i][j][k].cellState != CellState.active) {
                        grainGrowth.run = true;

                        int newId = grainGrowth.findNewId(i, j, k);

                        if (newId != 0) {
                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = grainGrowth.countDistance(grid.nextCellsList[i][j][k], i, j, k);
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
    }

    @Override
    public void decomposeAndExecuteFerrite(int task, int totalThreads, Grid grid, AusteniteFerriteTransformation austeniteFerriteTransformation) {
//        System.out.println("Dekompozycja blokowa fca");
        int w, h, d;

        if(totalThreads == 1) {
            h = 1;
            w = 1;
            d = 1;
        }

        else if(totalThreads == 2) {
            h = 2;
            w = 1;
            d = 1;
        }

        else if(totalThreads == 4) {
            h = 2;
            w = 2;
            d = 1;
        }

        else if(totalThreads == 8) {
            h = 2;
            w = 2;
            d = 2;
        }

        else if (totalThreads == 12) {
            h = 3;
            w = 2;
            d = 2;
        }
        else {
            h = grid.getHeight();
            w = grid.getWidth();
            d = grid.getDepth();

            System.out.println("błąd cube decompositon");
        }

        int cubeHeight = grid.getHeight() / h;
        int cubeWidth = grid.getWidth() / w;
        int cubeDepth = grid.getDepth() / d;

        int h_idx = (task % (totalThreads/d)) / w;
        int w_idx = task % w;
        int d_idx = task / (totalThreads/d);

        for (int i = h_idx * cubeHeight; i < (h_idx + 1) * cubeHeight; i++) {
            for (int j = w_idx * cubeWidth; j < (w_idx + 1) * cubeWidth; j++) {
                for (int k = d_idx * cubeDepth; k < (d_idx + 1) * cubeDepth; k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    int currentGrainID = grid.cellsList[i][j][k].idGrain;
                    Cell currentCell = grid.cellsList[i][j][k];

                    if (currentCell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(currentGrainID).getCarbonConcentration() < 0.77) {

                        int newId = austeniteFerriteTransformation.findNewId(i, j, k);

                        if (newId != currentGrainID) {

                            austeniteFerriteTransformation.run = true;

//                            currentCell?
                            grid.grainsList.get(grid.cellsList[i][j][k].idGrain).deleteCell(austeniteFerriteTransformation.ferrytCarbon);
                            grid.grainsList.get(newId).addCell(austeniteFerriteTransformation.ferrytCarbon);

                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = austeniteFerriteTransformation.countDistance(grid.nextCellsList[i][j][k], i, j, k);

                        }
                    }

                    if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                        austeniteFerriteTransformation.run = true;

                        if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).iteration) {
                            grid.nextCellsList[i][j][k].cellState = CellState.active;
                        }
                    }

                }
            }
        }
    }
}
