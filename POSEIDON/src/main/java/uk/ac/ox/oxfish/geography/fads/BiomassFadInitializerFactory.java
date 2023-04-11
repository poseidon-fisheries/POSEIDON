package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;

public abstract class BiomassFadInitializerFactory
    extends FadInitializerFactory<BiomassLocalBiology, BiomassAggregatingFad> {
    protected AbundanceFiltersFactory abundanceFiltersFactory;

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }
}
