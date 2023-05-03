package uk.ac.ox.oxfish.geography;


import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * an object containing basic info about where a far off port ought to be without
 */
public class FarOffPortInformation {

    private String portName = "FarOffPort";

    private int exitGridX = 5;

    private int exitGridY = 5;

    private double distanceFromExitInKm = 100;

    private double gasPriceAtPort = 0.1;

    /**
     * needs to keep track of this until it is initialized
     */
    private AlgorithmFactory<? extends Market> marketMaker = new FixedPriceMarketFactory();

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public void setExitGridX(int exitGridX) {
        this.exitGridX = exitGridX;
    }

    public void setExitGridY(int exitGridY) {
        this.exitGridY = exitGridY;
    }

    public void setDistanceFromExitInKm(double distanceFromExitInKm) {
        this.distanceFromExitInKm = distanceFromExitInKm;
    }

    public AlgorithmFactory<? extends Market> getMarketMaker() {
        return marketMaker;
    }

    public void setMarketMaker(AlgorithmFactory<? extends Market> marketMaker) {
        this.marketMaker = marketMaker;
    }

    public String getPortName() {
        return portName;
    }

    public int getExitGridX() {
        return exitGridX;
    }

    public int getExitGridY() {
        return exitGridY;
    }

    public double getDistanceFromExitInKm() {
        return distanceFromExitInKm;
    }


    public double getGasPriceAtPort() {
        return gasPriceAtPort;
    }

    public void setGasPriceAtPort(double gasPriceAtPort) {
        this.gasPriceAtPort = gasPriceAtPort;
    }
}
