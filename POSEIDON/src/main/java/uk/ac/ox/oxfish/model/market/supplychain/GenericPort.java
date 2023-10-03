package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericPort {
    double[] landings;


    public GenericPort(double[] landings){
        this.landings = landings;
    }

    public double[] getLandings() {
        return landings;
    }

    public double getLandings(int index) {
        return landings[index];
    }

    public void updateLandings(double[] newLandings){
        this.landings = newLandings;
    }

    public void updateLandings(int index, double newLandings){
        this.landings[index] = newLandings;
    }


}
