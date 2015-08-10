package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * like startable but assumed for a component of the fisher: will receive a reference from the fisher
 * Created by carrknight on 8/10/15.
 */
public interface FisherStartable {


    public void start(FishState model, Fisher fisher);

    public void turnOff();

}
