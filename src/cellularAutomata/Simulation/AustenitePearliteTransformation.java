//package cellularAutomata.Simulation;
//
//import cellularAutomata.Model.*;
//
//import java.util.*;
//
//public class AustenitePearliteTransformation extends Simulation{
//
//    private boolean run = true;
//
//    public AustenitePearliteTransformation(Grid grid) {
//        super(grid);
//    }
//
//    @Override
//    public void initGrains(){
//
//        grid.iterationSimulation = 0;
//
//        Map<Cell, Cell> tmpCellAssociation = aggregateAusteniteCellsIntoNewGrains();
//
//        grid.countBorderStateAustenitePearlite();
//        Map<Cell, List<Cell>> borderCellsForTmpGrain = new HashMap<>();
//
//        for(Cell cell: grid.cellsListOnTheBorder) {
//            Cell representativeCellForTmpGrain = find(tmpCellAssociation, cell);
//            borderCellsForTmpGrain.putIfAbsent(representativeCellForTmpGrain, new LinkedList<>());
//            borderCellsForTmpGrain.get(representativeCellForTmpGrain).add(cell);
//        }
//
//        Random random = new Random();
//        List<Cell> initializedCells = new LinkedList<>();
//        borderCellsForTmpGrain.values().forEach(borderCellList -> {
//            Cell perliteGrainInitialCell = borderCellList.get(random.nextInt(borderCellList.size()));
//            addNewPerliteGrain(perliteGrainInitialCell);
//            initializedCells.add(perliteGrainInitialCell);
//        });
//
//        grid.cellsListOnTheBorder.removeAll(initializedCells);
//
//        Collections.shuffle(grid.cellsListOnTheBorder);
//
//        int size = grid.cellsListOnTheBorder.size();
//
//        for (int i = 0; i < size * 0.05 - initializedCells.size(); i++) {
//            addNewPerliteGrain();
//        }
//    }
//
//    private Map<Cell, Cell> aggregateAusteniteCellsIntoNewGrains() {
//        // find and union implementation
//
//        // zmien na 3 wymiar
//        Map<Cell, Cell> findAndUnionStructure = new HashMap<>();
//        for(Cell[][] cellC : grid.cellsList) {
//            for(Cell[] cellRow : cellC) {
//                for (Cell cell : cellRow) {
//                    if (cell.getGrainType(grid) == GrainType.austenite) {
//                        findAndUnionStructure.put(cell, cell);
//                    }
//                }
//            }
//        }
//
//        for(Cell[][] cellC : grid.cellsList) {
//            for(Cell[] cellRow : cellC) {
//                for(Cell cell: cellRow) {
//                    if (cell.getGrainType(grid) == GrainType.austenite) {
//                        for (int i = -1; i <= 1; i++) {
//                            for (int j = -1; j <= 1; j++) {
//                                for (int k = -1; k <= 1; k++) {
//                                    int X = (grid.getHeight() + cell.getX() + i) % grid.getHeight();
//                                    int Y = (grid.getWidth() + cell.getY() + j) % grid.getWidth();
//                                    int Z = (grid.getDepth() + cell.getZ() + k) % grid.getDepth();
//
//                                    if (grid.cellsList[X][Y][Z].getGrainType(grid) == GrainType.austenite) {
//                                        union(findAndUnionStructure, cell, grid.cellsList[X][Y][Z]);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return findAndUnionStructure;
//    }
//
//    private Cell find(Map<Cell, Cell> parentStructure, Cell cell) {
//        if (cell.equals(parentStructure.get(cell))) {
//            return cell;
//        }
//        Cell parent = find(parentStructure, parentStructure.get(cell));
//        parentStructure.put(cell, parent);
//        return parent;
//    }
//
//    private void union(Map<Cell, Cell> parentStructure, Cell first, Cell second)  {
//        var parentFirst = find(parentStructure, first);
//        var parentSecond = find(parentStructure, second);
//        if (!parentFirst.equals(parentSecond)) {
//            parentStructure.put(parentFirst, parentSecond);
//        }
//    }
//
//    private void addNewPerliteGrain(Cell sourceCell){
////        System.out.println("add grain: " + grid.cellsListOnTheBorder.size());
//
//        Cell cell = sourceCell == null ? grid.cellsListOnTheBorder.get(0) : sourceCell;
//
//        int newId = grid.grainsList.size();
//        int oldID = grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain;
//        double carbon = grid.grainsList.get(oldID).getCarbonConcentration();
//
//        Grain grain = new Grain(newId, GrainType.pearlite, cell.getX(), cell.getY(), cell.getZ(),carbon);
//        grid.grainsList.put(newId, grain);
//        grid.grainsList.get(oldID).deleteCell(carbon);
//        grid.cellsList[cell.getX()][cell.getY()][cell.getZ()].idGrain = newId;
//        cell.cellState = CellState.active;
//
//        if(sourceCell == null) {
//            grid.cellsListOnTheBorder.remove(0);
//        }
//    }
//
//    private void addNewPerliteGrain(){
//        addNewPerliteGrain(null);
//    }
//
//
//    @Override
//    public void grow() {
//
//        for (int i = 0; i < grid.grainsList.size(); i++) {
//            grid.grainsList.get(i).iteration++;
//        }
//
//        this.run = false;
//
//        for (int i = 0; i < grid.getHeight(); i++) {
//            for (int j = 0; j < grid.getWidth(); j++) {
//                for (int k = 0; k < grid.getDepth(); k++) {
//
//                    grid.nextCellsList[i][j][k] = new Cell(grid.cellsList[i][j][k]);
//
//                    int currentGrainID = grid.cellsList[i][j][k].idGrain;
//                    Cell currentCell = grid.cellsList[i][j][k];
//                    double carbon = grid.grainsList.get(currentGrainID).getCarbonConcentration();
//
//                    if (currentCell.getGrainType(grid) == GrainType.austenite) {
//
//                        this.run = true;
//
//                        int newId = findNewId(i, j, k);
//
//                        if (newId != currentGrainID) {
//
//                            grid.grainsList.get(grid.cellsList[i][j][k].idGrain).deleteCell(carbon);
//                            grid.grainsList.get(newId).addCell(carbon);
//
//                            grid.nextCellsList[i][j][k].cellState = CellState.pending;
//                            grid.nextCellsList[i][j][k].idGrain = newId;
//                            grid.nextCellsList[i][j][k].time = countDistance(grid.nextCellsList[i][j][k], i, j, k);
//
//                        }
//                    }
//
//                    if (grid.nextCellsList[i][j][k].cellState == CellState.pending) {
//
//                        this.run = true;
//
//                        if (((int) Math.ceil(grid.nextCellsList[i][j][k].time)) <= grid.grainsList.get(grid.nextCellsList[i][j][k].idGrain).iteration) {
//                            grid.nextCellsList[i][j][k].cellState = CellState.active;
//                        }
//                    }
//                }
//            }
//        }
//
//        grid.cellsList = grid.nextCellsList;
//
//        grid.iterationSimulation++;
////        System.out.println("iteracja symulacji: " + grid.iterationSimulation);
//    }
//
//    @Override
//    public int findNewId(int x, int y, int z) {
////        sąsiedztwo moore'a
//
//        for (int i = -1; i <= 1; i++) {
//            for (int j = -1; j <= 1; j++) {
//                for (int k = -1; k <= 1; k++) {
////                warunki brzegowe periodyczne
//                    int X = (grid.getHeight() + x + i) % grid.getHeight();
//                    int Y = (grid.getWidth() + y + j) % grid.getWidth();
//                    int Z = (grid.getDepth() + z + k) % grid.getDepth();
//
//                    if (grid.cellsList[X][Y][Z].getGrainType(grid) == GrainType.pearlite && grid.cellsList[X][Y][Z].cellState == CellState.active) {
//                        return grid.cellsList[X][Y][Z].idGrain;
//                    }
//                }
//            }
//        }
//        return grid.cellsList[x][y][z].idGrain;
//    }
//
//    public boolean isRun() {
//        return run;
//    }
//}
