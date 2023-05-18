package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

public interface FishValueCalculator {

    default double valueOf(final Catch catchesKept, final double[] prices) {
        return valueOf(catchesKept.getBiomassArray(), prices);
    }

    double valueOf(double[] biomasses, double[] prices);

    default double valueOf(final LocalBiology biology, final double[] prices) {
        final double[] biomasses =
            getGlobalBiology().getSpecies().stream()
                .mapToDouble(biology::getBiomass)
                .toArray();
        return valueOf(biomasses, prices);
    }

    GlobalBiology getGlobalBiology();


}
