package cellularAutomata.Visualization;

import Utils.Observer;
import cellularAutomata.Model.Cell;
import cellularAutomata.Model.GrainType;
import cellularAutomata.Model.Grid;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Visualizer implements Observer {

    private Grid grid;
    private GraphicsContext graphics;
    private Canvas canvas;
    private int visScale;
    private Random random;
    private Map<Integer, Color> austenitColor;
    private Map<Integer, Color> ferrytColor;
//    private Map<Integer, Color> perliteColor;


    @Override
    public void onValueChanged() {
        showGrid();
    }

    public Visualizer(Grid grid, Canvas canvas, int visScale) {
        this.grid = grid;
        this.canvas = canvas;
        this.graphics = this.canvas.getGraphicsContext2D();
        this.random = new Random();
        this.austenitColor = new HashMap<>();
        this.ferrytColor = new HashMap<>();
//        this.perliteColor = new HashMap<>();

        this.visScale = visScale;
    }

//    chyba ok
    private void showGrid() {
        canvasClear();

////        do testów
//        int austenitL = 0;
//        int ferrytL = 0;
//
//        for (int i = 1; i < grid.grainsList.size(); i++) {
//            if (grid.getGrainType(i) == GrainType.ferrite) ferrytL++;
//            else if (grid.getGrainType(i) == GrainType.austenite) austenitL++;
//        }
//
//        System.out.println("ferryt: " + ferrytL + ", austenit: " + austenitL);
//        System.out.println("liczba ziaren: " + (grid.grainsList.size() - 1));

        int i, j, k = 0;

        if (grid.getDepth() == 1) {
            k = 0;
            for (i = 0; i < grid.getHeight(); i++) {
                for (j = 0; j < grid.getWidth(); j++) {
                    if (grid.cellsList[i][j][k].idGrain != 0 & grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.austenite) {
                        austenitColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.red());
                        graphics.setFill(austenitColor.get(grid.cellsList[i][j][k].idGrain));
                        graphics.fillRect(j * visScale, i * visScale, visScale, visScale);
                    }

                    else if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.ferrite) {
                        ferrytColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.yellow());
                        graphics.setFill(ferrytColor.get(grid.cellsList[i][j][k].idGrain));
                        graphics.fillRect(j * visScale, i * visScale, visScale, visScale);
                    }
                }
            }
            graphics.setFill(Color.rgb(50, 50, 50));
        } else {
            //for z = 0 & z = depth - 1;
            k = 0;
            j = 0;
            i = 0;

            for (i = 0; i < grid.getHeight(); i++) {
                for (j = 0; j < grid.getWidth(); j++) {
                    k = 0;
//                    A
                    if (grid.cellsList[i][j][k].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                        else if(grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.ferrite){
                            ferrytColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                    }
//                  F
                    k = grid.getDepth() - 1;
                    if (grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain));
                            graphics.fillRect(((grid.getWidth() - j - 1) + 2 * grid.getDepth() + grid.getWidth() + 3) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                        else if (grid.getGrainType(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain) == GrainType.ferrite) {
//                            System.out.println("ferrite");
                            ferrytColor.putIfAbsent(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][grid.getWidth() - j - 1][k].idGrain));
                            graphics.fillRect(((grid.getWidth() - j - 1) + 2 * grid.getDepth() + grid.getWidth() + 3) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                    }
                }
            }

            for (i = 0; i < grid.getHeight(); i++) {
                for (k = 0; k < grid.getDepth(); k++) {
                    j = 0;
//                    C
                    if (grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain));
                            graphics.fillRect((grid.getDepth() - k - 1) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                        else if (grid.getGrainType(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain) == GrainType.ferrite) {
                            ferrytColor.putIfAbsent(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain));
                            graphics.fillRect((grid.getDepth() - k - 1) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                    }

                    j = grid.getWidth() - 1;
                    if (grid.cellsList[i][j][k].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((k + 1 * grid.getDepth() + grid.getWidth() + 2) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
//                        E
                        else if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.ferrite) {
                            ferrytColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((k + 1 * grid.getDepth() + grid.getWidth() + 2) * visScale, (i + grid.getDepth() + 1) * visScale, visScale, visScale);
                        }
                    }
                }
            }

            for (j = 0; j < grid.getWidth(); j++) {
                for (k = 0; k < grid.getDepth(); k++) {
                    i = 0;
//                    D
                    if (grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (grid.getDepth() - k - 1) * visScale, visScale, visScale);
                        }
                        else if (grid.getGrainType(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain) == GrainType.ferrite) {
                            ferrytColor.putIfAbsent(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][j][grid.getDepth() - k - 1].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (grid.getDepth() - k - 1) * visScale, visScale, visScale);
                        }
                    }

//                    B
                    i = grid.getHeight() - 1;
                    if (grid.cellsList[i][j][k].idGrain != 0) {
                        if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.austenite) {
                            austenitColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.red());
                            graphics.setFill(austenitColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (k + grid.getDepth() + grid.getHeight() + 2) * visScale, visScale, visScale);
                        }
                        else if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.ferrite) {
                            ferrytColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.yellow());
                            graphics.setFill(ferrytColor.get(grid.cellsList[i][j][k].idGrain));
                            graphics.fillRect((j + grid.getDepth() + 1) * visScale, (k + grid.getDepth() + grid.getHeight() + 2) * visScale, visScale, visScale);
                        }
                    }
                }
            }

//            graphics.setFill(Color.rgb(50, 50, 50));
//            dodatkowo wyświetlenie środka do testów
            k = (grid.getDepth()-1)/2;
            for (i = 0; i < grid.getHeight(); i++) {
                for (j = 0; j < grid.getWidth(); j++) {
                    if (grid.cellsList[i][j][k].idGrain != 0 & grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.austenite) {
                        austenitColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.red());
                        graphics.setFill(austenitColor.get(grid.cellsList[i][j][k].idGrain));
                        graphics.fillRect((j+grid.getDepth()+grid.getDepth()+grid.getWidth()+3) * visScale, (i+grid.getDepth()+grid.getHeight()+2) * visScale, visScale, visScale);
                    }

                    else if (grid.getGrainType(grid.cellsList[i][j][k].idGrain) == GrainType.ferrite) {
                        ferrytColor.putIfAbsent(grid.cellsList[i][j][k].idGrain, this.yellow());
                        graphics.setFill(ferrytColor.get(grid.cellsList[i][j][k].idGrain));
                        graphics.fillRect((j+grid.getDepth()+grid.getDepth()+grid.getWidth()+3) * visScale, (i+grid.getDepth()+grid.getHeight()+2) * visScale, visScale, visScale);
                    }
                }
            }
            graphics.setFill(Color.rgb(50, 50, 50));

        }
    }

    private Color yellow(){
        return Color.rgb(255, random.nextInt(55)+200, 0);
    }

    private Color blue(){
        return Color.rgb(random.nextInt(50), random.nextInt(50), random.nextInt(100)+155);
    }

    private Color red(){
//        return Color.rgb(random.nextInt(100)+155, random.nextInt(50), random.nextInt(50));
        return Color.rgb(random.nextInt(100)+105, random.nextInt(100), random.nextInt(100));

    }

//    3D
    public void printBorder(){
        grid.countBorderForAll();
        graphics.setFill(Color.rgb(10, 10, 10));
        for(Cell cell : grid.allCellsOnTheBorder){
            if(grid.getDepth()==1) {
                graphics.fillRect(cell.getY() * visScale, cell.getX() * visScale, visScale, visScale);
            }
            else if(cell.getX() == 0) {
//                graphics.setFill(Color.rgb(10, 10, 10));
                graphics.fillRect((cell.getY() + grid.getDepth() + 1) * visScale, (cell.getZ()) * visScale, visScale, visScale);
            }
            else if(cell.getX() == grid.getHeight()-1){
                graphics.fillRect((cell.getY() + grid.getDepth() + 1) * visScale, (cell.getZ() + grid.getDepth() + grid.getHeight() + 2) * visScale, visScale, visScale);
            }
            else if(cell.getY() == 0){
                graphics.fillRect((cell.getZ()) * visScale, (cell.getX() + grid.getDepth() + 1) * visScale, visScale, visScale);
            }
            else if(cell.getY() == grid.getWidth()-1){graphics.fillRect((cell.getZ() + 1 * grid.getDepth() + grid.getWidth() + 2) * visScale, (cell.getX() + grid.getDepth() + 1) * visScale, visScale, visScale);}
            else if(cell.getZ() == 0){graphics.fillRect((cell.getY() + grid.getDepth() + 1) * visScale, (cell.getX() + grid.getDepth() + 1) * visScale, visScale, visScale);}
            else if(cell.getZ() == grid.getDepth()-1){graphics.fillRect(((cell.getY()) + 2 * grid.getDepth() + grid.getWidth() + 3) * visScale, (cell.getX() + grid.getDepth() + 1) * visScale, visScale, visScale);}
            else if(cell.getZ() == (grid.getDepth()-1)/2){
                System.out.println("border w połowie: (" + cell.getX() + ", " + cell.getY() + ", " + cell.getZ()+")");
                graphics.fillRect((cell.getY()+grid.getDepth()+grid.getDepth()+grid.getWidth()+3) * visScale, (cell.getX()+grid.getDepth()+grid.getHeight()+2) * visScale, visScale, visScale);
            }
        }

    }

    public void canvasClear(){
//        graphics.clearRect(0, 0, grid.getHeight()*visScale, grid.getWidth()*visScale);
//        graphics.clearRect(0, 0, (grid.getHeight()+grid.getDepth()+ grid.getDepth())*visScale, (grid.getWidth()+grid.getWidth()+grid.getDepth()+ grid.getDepth())*visScale);
//        graphics.clearRect(0, 0,(grid.getWidth()+grid.getWidth()+grid.getDepth()+ grid.getDepth())*visScale, (grid.getHeight()+grid.getDepth()+ grid.getDepth())*visScale);
        graphics.clearRect(0, 0,canvas.getWidth(), canvas.getHeight());

    }

}
