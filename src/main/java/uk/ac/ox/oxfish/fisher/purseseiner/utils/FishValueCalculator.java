package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

public interface FishValueCalculator {
    double valueOf(Catch catchesKept);

    double valueOf(double[] biomass);

    double valueOf(LocalBiology biology);
}
