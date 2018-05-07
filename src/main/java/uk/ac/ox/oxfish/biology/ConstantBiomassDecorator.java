package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * intercepts and doesn't pass along "reactTo" calls
 */
public class ConstantBiomassDecorator implements LocalBiology {


    private final LocalBiology delegate;

    public ConstantBiomassDecorator(LocalBiology delegate) {
        this.delegate = delegate;
    }


    @Override
    public Double getBiomass(Species species) {
        return delegate.getBiomass(species);
    }

    @Override
    public void reactToThisAmountOfBiomassBeingFished(Catch caught, Catch notDiscarded, GlobalBiology biology) {
        //neutralized!
    }

    @Override
    public StructuredAbundance getAbundance(Species species) {
        return delegate.getAbundance(species);
    }

    @Override
    public void start(FishState model) {
        delegate.start(model);
    }

    @Override
    public void turnOff() {
        delegate.turnOff();
    }
}
