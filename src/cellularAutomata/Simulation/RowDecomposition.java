package cellularAutomata.Simulation;

import cellularAutomata.Model.Cell;
import cellularAutomata.Model.CellState;
import cellularAutomata.Model.Grid;

import java.util.ArrayList;
import java.util.List;

public class RowDecomposition implements ParallelDecomposition {
    @Override
    public void decomposeAndExecute(int task, int totalThreads, Grid grid, GrainGrowth grainGrowth) {

        for (int i = task * grid.getHeight() / totalThreads; i < (task + 1) * grid.getHeight() / totalThreads; i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

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
}
