package cellularAutomata.Simulation;

import cellularAutomata.Model.Grid;

public interface ParallelDecomposition {
    void decomposeAndExecute(int threadId, int totalThread, Grid grid, GrainGrowth grainGrowth);
}
