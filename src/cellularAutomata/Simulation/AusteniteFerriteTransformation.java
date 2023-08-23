package cellularAutomata.Simulation;

import cellularAutomata.Model.*;

import java.util.*;

public class AusteniteFerriteTransformation extends Simulation{
    public double ferrytCarbon = 0.0218;
    private double numberOfFerriteGrains;
    private boolean run = true;


    public AusteniteFerriteTransformation(Grid grid) {
        super(grid);
    }

    @Override
    public void initGrains() {

        grid.countBorderStateAustenit();
        Collections.shuffle(grid.cellsListOnTheBorder);

//        numberOfFerriteGrains = grid.cellsListOnTheBorder.size() * 0.1; //domyślnie
        numberOfFerriteGrains = grid.cellsListOnTheBorder.size() * 0.01;

        System.out.println("number of ferrite: " + numberOfFerriteGrains );

        for (int i = 0; i < numberOfFerriteGrains; ) {

            if (grid.cellsListOnTheBorder.isEmpty()) break;

            Cell cell = grid.cellsListOnTheBorder.get(0);

            if(cell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(cell.idGrain).getCarbonConcentration() < 0.77) {
                int newId = grid.grainsList.size();
                int currentId = grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain;

                Grain grain = new Grain(newId, GrainType.ferrite, cell.getX(), cell.getY(), cell.getZ(), this.ferrytCarbon);
                grain.setParentGrain(currentId);
                grid.grainsList.put(newId, grain);

//                w deleteCell od razu modyfikowane jest stężenie węgla
                grid.grainsList.get(currentId).deleteCell(this.ferrytCarbon);
                grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain = newId;

                i++;
            }

            grid.cellsListOnTheBorder.remove(0);
        }
    }

    @Override
    public void grow(){

//        po co każde ziarno ma swoją iterację?
        for(int i = 0; i < grid.grainsList.size(); i++) {
            grid.grainsList.get(i).iteration++;
        }

        this.run = false;

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);

                    int currentGrainID = grid.cellsList[i][j][k].idGrain;
                    Cell currentCell = grid.cellsList[i][j][k];

                    if (currentCell.getGrainType(grid) == GrainType.austenite && grid.grainsList.get(currentGrainID).getCarbonConcentration() < 0.77) {

                        int newId = findNewId(i, j, k);

                        if (newId != currentGrainID) {

                            this.run = true;

//                            currentCell?
                            grid.grainsList.get(grid.cellsList[i][j][k].idGrain).deleteCell(this.ferrytCarbon);
                            grid.grainsList.get(newId).addCell(this.ferrytCarbon);

                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
                            grid.nextCellsList[i][j][k].idGrain = newId;
                            grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);

                        }
                    }

                    if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {

                        this.run = true;

                        if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).iteration) {
                            grid.nextCellsList[i][j][k].cellState = CellState.active;
                        }
                    }
                }
            }
        }

        grid.cellsList = grid.nextCellsList;

        grid.iterationSimulation++;
//        System.out.println("iteracja symulacji: " + grid.iterationSimulation);
    }

    @Override
    public int findNewId(int x, int y, int z) {
//        sąsiedztwo moore'a

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
//                warunki brzegowe periodyczne
                    int X = (grid.getHeight() + x + i) % grid.getHeight();
                    int Y = (grid.getWidth() + y + j) % grid.getWidth();
                    int Z = (grid.getDepth() + z + k) % grid.getDepth();

                    if ((grid.cellsList[x][y][z].idGrain == grid.grainsList.get(grid.cellsList[X][Y][Z].idGrain).getParentGrain()) && (grid.cellsList[X][Y][Z].cellState == CellState.active)) {
                        return grid.cellsList[X][Y][Z].idGrain;
                    }
                }
            }
        }
        return grid.cellsList[x][y][z].idGrain;
    }

    public void addCellState() {
//        ziarno znajduje się na granicy jeśli jest austenitem (bo komórka austenitu zamienia się w perlit)
//        oraz przynajmnej jeden z jego sąsiadów jest perlitem

        grid.cellsListOnTheBorder = new ArrayList<>();

        for (int i = 0; i < grid.getHeight(); i++) {
            for (int j = 0; j < grid.getWidth(); j++) {
                for (int k = 0; k < grid.getDepth(); k++) {

                    if (grid.cellsList[i][j][k].getGrainType(grid) == GrainType.austenite) {

                        if (ferriteInNeighbourhood(i, j, k)) {
                            grid.cellsListOnTheBorder.add(grid.cellsList[i][j][k]);
                        }
                    }
                }
            }
        }
    }

    private boolean ferriteInNeighbourhood(int i, int j, int k){
        for (int m = -1; m <= 1; m++) {
            for (int n = -1; n <= 1; n++) {
                for (int o = -1; o <= 1; o++) {
                    int X = (grid.getHeight() + i + m) % grid.getHeight();
                    int Y = (grid.getWidth() + j + n) % grid.getWidth();
                    int Z = (grid.getDepth() + k + o) % grid.getDepth();
//                granica austenit-austenit oraz austenit-ferrt
                    if (grid.cellsList[i][j][k].idGrain != grid.cellsList[X][Y][Z].idGrain) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isRun() {
        return run;
    }

}
