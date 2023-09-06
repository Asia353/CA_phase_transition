package cellularAutomata.Simulation;

import cellularAutomata.Model.*;

import java.util.HashMap;
import java.util.Map;

public class NeighborDominantGrain implements NeighborhoodSearchPattern{

    public Grid grid;
    public GrainGrowth grainGrowth;

    public NeighborDominantGrain(Grid grid, GrainGrowth grainGrowth){
        this.grid = grid;
        this.grainGrowth = grainGrowth;
    }

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
                        neighbours.putIfAbsent(grid.cellsList[X][Y][Z].idGrain, grainGrowth.countDistance(grid.cellsList[X][Y][Z], x, y, z));
                    }
                }
            }
        }
        var optMinGrain = neighbours.entrySet().stream().min((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1);
        return optMinGrain.map(Map.Entry::getKey).orElse(0);
    }

    @Override
    public int findNewIdFCA(int x, int y, int z) {
        return 0;
    }
}
