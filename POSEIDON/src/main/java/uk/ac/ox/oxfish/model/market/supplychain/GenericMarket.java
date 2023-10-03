package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericMarket {
    String name;
    double[] demandLoin;
    double[] demandPackaged;

    public GenericMarket(String name,
                         double[] demandLoin,
                         double[] demandPackaged){
        this.name = name;
        this.demandLoin = demandLoin;
        this.demandPackaged = demandPackaged;
    }

    public double[] getDemandLoin() {
        return demandLoin;
    }

    public double[] getDemandPackaged() {
        return demandPackaged;
    }

    public double getDemandLoin(int index){
        return demandLoin[index];
    }

    public double getDemandPackaged(int index){
        return demandPackaged[index];
    }

    public String getName(){
        return name;
    }
}
