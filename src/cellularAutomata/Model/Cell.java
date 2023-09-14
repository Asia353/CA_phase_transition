package cellularAutomata.Model;
import java.util.Objects;

public class Cell {
    public int idGrain;
    public CellState cellState;
    public double time;

    private int x;
    private int y;
    private int z;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x && y == cell.y && z == cell.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public Cell(int x, int y, int z){
        this.idGrain = 0;
        this.cellState = CellState.notAlive;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Cell(int x, int y, int z, int idGrain){
        this.idGrain = idGrain;
        this.cellState = CellState.alive;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Cell (Cell cell){
        this.cellState = cell.cellState;
        this.time = cell.time;
        this.idGrain = cell.idGrain;
        this.x = cell.x;
        this.y = cell.y;
        this.z = cell.z;
    }

    public GrainType getGrainType(Grid grid) {
        return grid.grainsList.get(this.idGrain).getGrainType();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

}
