package uk.ac.ox.oxfish.model.market.supplychain;

public class GenericImportTariff {
    private int origin;
    private int destination;
    private double[] rate_raw, rate_loin, rate_packaged;

    public GenericImportTariff(int origin, int destination, double[] rate_raw, double[] rate_loin, double[] rate_packaged){
        this.origin=origin;
        this.destination=destination;
        this.rate_raw=rate_raw;
        this.rate_loin=rate_loin;
        this.rate_packaged=rate_packaged;
    }

    public double getRate_loin(int s) {
        return rate_loin[s];
    }

    public double getRate_packaged(int s) {
        return rate_packaged[s];
    }

    public double getRate_raw(int s) {
        return rate_raw[s];
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public void updateRates(int s, double raw, double loin, double packaged){
        this.rate_raw[s]=raw;
        this.rate_loin[s] = loin;
        this.rate_packaged[s]=packaged;
    }

}
