package cellularAutomata.Simulation;

import cellularAutomata.Model.CellState;
import cellularAutomata.Model.Grid;

public class NeighborFirstGrain implements NeighborhoodSearchPattern {

    public Grid grid;
    public GrainGrowth grainGrowth;

    public NeighborFirstGrain(Grid grid, GrainGrowth grainGrowth){
        this.grid = grid;
        this.grainGrowth = grainGrowth;
    }

    @Override
    public int findNewId(int x, int y, int z) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {

                    int X = (grid.getHeight() + x + i) % grid.getHeight();
                    int Y = (grid.getWidth() + y + j) % grid.getWidth();
                    int Z = (grid.getDepth() + z + k) % grid.getDepth();

                    if (grid.cellsList[X][Y][Z].cellState == CellState.alive) {
                        return grid.cellsList[X][Y][Z].idGrain;
                    }
                }
            }
        }
        return 0;
    }
}
