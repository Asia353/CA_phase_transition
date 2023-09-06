package cellularAutomata.Simulation;

import cellularAutomata.Model.Grid;

public interface NeighborhoodSearchPattern {
    int findNewId(int x, int y, int z);
    int findNewIdFCA(int x, int y, int z);
}
