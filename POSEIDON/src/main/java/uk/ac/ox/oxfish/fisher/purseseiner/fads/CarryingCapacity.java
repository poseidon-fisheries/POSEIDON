package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.Species;

public interface CarryingCapacity {

    double getTotal();

    boolean isFull(
        Fad fad,
        Species species
    );
}
