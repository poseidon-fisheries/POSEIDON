package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simplest possible gas price maker: sets an initial price and doesn't manage it any further
 * Created by carrknight on 7/18/17.
 */
public class FixedGasPrice implements GasPriceMaker {


    private final double price;

    public FixedGasPrice(double price) {
        this.price = price;
    }

    @Override
    public double supplyInitialPrice(SeaTile location, String portName) {
        return price;
    }

    @Override
    public void start(Port port, FishState model) {
        //ignored
    }
}
