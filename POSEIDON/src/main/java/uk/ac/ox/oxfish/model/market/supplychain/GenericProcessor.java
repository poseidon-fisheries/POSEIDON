package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericProcessor {
    String name;
    String location;
    int locationIndex;
    double[] maxOutput;  //index 0: loining capacity
                        // index 1: canning(packaging) capacity

    //cannery transformation ability
    double[] transformationAbility;
    double[] processingCost;

    public GenericProcessor(double[] maxOutput,
                            double[] transformationAbility,
                            double[] processingCost){
        this.maxOutput = maxOutput;
        this.transformationAbility = transformationAbility;
        this.processingCost = processingCost;
    }

    public GenericProcessor(String name,
                            String location,
                            double[] maxOutput,
                            double[] transformationAbility,
                            double[] processingCost){
        this.name = name;
        this.location = location;
        this.maxOutput = maxOutput;
        this.transformationAbility = transformationAbility;
        this.processingCost = processingCost;
    }

    public GenericProcessor(String name,
                            String location,
                            int locationIndex,
                            double[] maxOutput,
                            double[] transformationAbility,
                            double[] processingCost){
        this.name = name;
        this.location = location;
        this.locationIndex = locationIndex;
        this.maxOutput = maxOutput;
        this.transformationAbility = transformationAbility;
        this.processingCost = processingCost;
    }

    public GenericProcessor(String name,
                            double[] maxOutput,
                            double[] transformationAbility,
                            double[] processingCost){
        this.name = name;
        this.maxOutput = maxOutput;
        this.transformationAbility = transformationAbility;
        this.processingCost = processingCost;
    }

    public void setMaxOutput(double[] maxOutput){
        this.maxOutput = maxOutput;
    }

    public double[] getMaxOutput(){
        return maxOutput;
    }
    public double[] getTransformationAbility(){
        return transformationAbility;
    }
    public double[] getProcessingCost(){
        return processingCost;
    }
    public double getMaxOutput(int index){
        return maxOutput[index];
    }
    public double getTransformationAbility(int index){
        return transformationAbility[index];
    }
    public double getProcessingCost(int index){
        return processingCost[index];
    }

    public String getName(){
        return name;
    }
    public int getLocationIndex(){return locationIndex; }

    public String getLocation() {return location;
    }
}
