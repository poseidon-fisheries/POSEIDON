package uk.ac.ox.oxfish.model.market.supplychain;

public class TransportCost {
    int origin;
    int destination;
    double cost;

    public TransportCost(int origin, int destination, double cost){
        this.origin=origin;
        this.destination=destination;
        this.cost=cost;
    }

    public int getOrigin(){
        return origin;
    }
    public int getDestination(){
        return destination;
    }

    public double getCost() {
        return cost;
    }
}
