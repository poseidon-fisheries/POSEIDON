package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericMarket {

    double[] demandLoin;
    double[] demandPackaged;

    public GenericMarket(double[] demandLoin,
                         double[] demandPackaged){
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
}
