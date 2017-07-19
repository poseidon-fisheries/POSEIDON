package uk.ac.ox.oxfish.model.market.gas;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Any class that sets (and possibly updates) gas prices
 * Created by carrknight on 7/18/17.
 */
public interface GasPriceMaker {



    public  double supplyInitialPrice(SeaTile location,String portName);

    public void start(Port port, FishState model);




}
