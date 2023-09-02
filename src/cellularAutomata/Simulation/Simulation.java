package cellularAutomata.Simulation;

import cellularAutomata.Model.Cell;
import cellularAutomata.Model.Grid;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class Simulation {
    protected Grid grid;

    public Simulation(Grid grid){
        this.grid = grid;
    }

    public abstract int findNewId(int x, int y, int z);
    public abstract void grow();
    public abstract void initGrains();

    protected double countDistance(Cell cellActive, int xCurrent, int yCurrent, int zCurrent) {
        int activeCellX = grid.grainsList.get(cellActive.idGrain).parentX;
        int activeCellY = grid.grainsList.get(cellActive.idGrain).parentY;
        int activeCellZ = grid.grainsList.get(cellActive.idGrain).parentZ;

        List<Double> distances = new LinkedList<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int targetX = xCurrent + dx * grid.getHeight();
                    int targetY = yCurrent + dy * grid.getWidth();
                    int targetZ = zCurrent + dz * grid.getDepth();
                    distances.add(Math.sqrt(Math.pow(activeCellX - targetX, 2) + Math.pow(activeCellY - targetY, 2) + Math.pow(activeCellZ - targetZ, 2)));
                }
            }
        }
        return distances.stream().min(Comparator.naturalOrder()).orElse(Double.POSITIVE_INFINITY);
    }
}
