package uk.ac.ox.poseidon.common.api;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

public interface ModelState {
    MersenneTwisterFast getRandom();

    MapExtent getMapExtent();
}
