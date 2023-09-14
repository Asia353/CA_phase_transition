package cellularAutomata.Simulation;

import cellularAutomata.Model.Cell;
import cellularAutomata.Model.CellState;
import cellularAutomata.Model.GrainType;
import cellularAutomata.Model.Grid;

public class ColumnDecomposition implements ParallelDecomposition {
    @Override
    public void decomposeAndExecute(int task, int totalThreads, Grid grid, GrainGrowth grainGrowth) {
//        System.out.println("Dekompozycja kolumnowa rozrost");
        for (int j = task * grid.getWidth() / totalThreads; j < (task + 1) * grid.getWidth() / totalThreads; j++) {
            for (int i = 0; i < grid.getHeight(); i++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    if (grid.cellsList[i][j][k].cellState != CellState.alive) {
                        grainGrowth.run = true;

                        int newId = grainGrowth.findNewId(i, j, k);

                        if (newId != 0) {
                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = grainGrowth.countDistance(grid.nextCellsList[i][j][k], i, j, k);
                        }

                        if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                            if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.iterationSimulation) {
                                grid.nextCellsList[i][j][k].cellState = CellState.alive;
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
//        System.out.println("Dekompozycja kolumnowa fca");
        for (int j = task * grid.getWidth() / totalThreads; j < (task + 1) * grid.getWidth() / totalThreads; j++) {
            for (int i = 0; i < grid.getHeight(); i++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    int currentGrainID = grid.cellsList[i][j][k].idGrain;
                    Cell currentCell = grid.cellsList[i][j][k];

                    if (currentCell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(currentGrainID).getCarbonConcentration() < 0.77) {

                        int newId = austeniteFerriteTransformation.findNewId(i, j, k);

                        if (newId != currentGrainID) {

                            austeniteFerriteTransformation.run = true;

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
                            grid.nextCellsList[i][j][k].cellState = CellState.alive;
                        }
                    }
                }
            }
        }
    }
}

