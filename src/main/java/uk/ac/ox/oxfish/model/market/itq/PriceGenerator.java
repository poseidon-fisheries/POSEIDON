package uk.ac.ox.oxfish.model.market.itq;

import uk.ac.ox.oxfish.model.FisherStartable;

import java.io.Serializable;

/**
 * Used by a fisher to discover the price (lambda) that makes him indifferent
 * between buying and not buying quotas
 * Created by carrknight on 10/7/15.
 */
public interface PriceGenerator extends FisherStartable, Serializable{


    double computeLambda();

}
