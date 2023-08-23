package cellularAutomata.Model;

public class Grain {
    private int id;
    public int parentX;
    public int parentY;
    public int parentZ;
    private GrainType grainType;
    private double carbonConcentration;
    private double carbonAll;
    public int iteration;
    public int numberOfCells;
    private int parentGrain; // potrzebne w przemianie austenit ferryt, gdy potrzebujemy zapamiętać ziarno w które ma się wrastać nowo powstały ferryt

    public Grain(int id, GrainType grainType, int x, int y, int z, double carbonConcentration) {
        this.numberOfCells = 1;
        this.grainType = grainType;
        this.id = id;
        this.parentX = x;
        this.parentY = y;
        this.parentZ = z;
        this.carbonConcentration = carbonConcentration;
        this.carbonAll = this.carbonConcentration * this.numberOfCells;
        this.iteration = 0;
        this.parentGrain = 0;
    }

    public void changeInitAustenitCarbon(double carbon){
        this.carbonConcentration = carbon;
        this.countCarbonAll();
    }

    public void countCarbonAll(){
        this.carbonAll = this.numberOfCells * this.carbonConcentration;
    }

    public void correctCarbon(double carbon) {
        this.carbonAll = this.carbonAll - carbon;
        this.carbonConcentration = this.carbonAll / this.numberOfCells;
    }


    public GrainType getGrainType() {
        return grainType;
    }

    public void addCell(double carbon){
        this.numberOfCells++;
        this.carbonAll = this.carbonAll + carbon;
    }

    public void deleteCell(double carbon){
        this.numberOfCells--;
        this.correctCarbon(carbon);
    }

    public double getCarbonConcentration() {
        return this.carbonConcentration;
    }

    public double getCarbonAll() {
        return carbonAll;
    }

    public int getParentGrain() {
        return parentGrain;
    }

    public void setParentGrain(int parentGrain) {
        this.parentGrain = parentGrain;
    }
}


