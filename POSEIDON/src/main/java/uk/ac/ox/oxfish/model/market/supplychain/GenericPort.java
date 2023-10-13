package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericPort {
    String name;
    String location;
    int locationIndex;
    double[] landings;


    public GenericPort(String name, double[] landings){
        this.name = name;
        this.landings = landings;
    }

    public GenericPort(String name, String location, double[] landings){
        this.name = name;
        this.location = location;
        this.landings = landings;
    }

    public GenericPort(String name, String location, int locationIndex, double[] landings){
        this.name = name;
        this.location = location;
        this.locationIndex = locationIndex;
        this.landings = landings;
    }


    public double[] getLandings() {
        return landings;
    }

    public String getName(){
        return this.name;
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

    public int getLocationIndex(){return locationIndex;}

    public String getLocation(){return location;}

}
