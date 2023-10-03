package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericProcessor {

    double[] maxOutput;

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

}
