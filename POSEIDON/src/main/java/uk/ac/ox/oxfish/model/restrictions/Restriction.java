package uk.ac.ox.oxfish.model.restrictions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;

public interface Restriction extends FisherStartable {

    boolean canFishHere(Fisher agent, SeaTile tile, FishState model);
}
