package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericMarket {
    String name;
    int locationIndex;
    String location;
    double[] demandLoin; //Index 3 for exchangeable
    double[] demandPackaged;

    public GenericMarket(String name,
                         double[] demandLoin,
                         double[] demandPackaged){
        this.name = name;
        this.demandLoin = demandLoin;
        this.demandPackaged = demandPackaged;
    }
    public GenericMarket(String name,
                         String location,
                         double[] demandLoin,
                         double[] demandPackaged){
        this.name = name;
        this.location = location;
        this.demandLoin = demandLoin;
        this.demandPackaged = demandPackaged;
    }

    public GenericMarket(String name,
                         String location,
                         int locationIndex,
                         double[] demandLoin,
                         double[] demandPackaged){
        this.name = name;
        this.location = location;
        this.locationIndex = locationIndex;
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
    public String getLocation(){return location;}

    public int getLocationIndex(){ return locationIndex;}
}
