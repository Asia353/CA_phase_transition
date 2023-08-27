package cellularAutomata.Simulation;

import cellularAutomata.Model.Grid;

public interface ParallelDecomposition {
    void decomposeAndExecute(int task, int totalThread, Grid grid, GrainGrowth grainGrowth);
    void decomposeAndExecuteFerrite(int task, int totalThread, Grid grid, AusteniteFerriteTransformation austeniteFerriteTransformation);
}
