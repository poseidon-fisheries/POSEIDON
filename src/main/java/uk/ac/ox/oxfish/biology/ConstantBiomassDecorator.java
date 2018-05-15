package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * intercepts and doesn't pass along "reactTo" calls
 */
public class ConstantBiomassDecorator implements VariableBiomassBasedBiology {


    private final VariableBiomassBasedBiology delegate;

    public ConstantBiomassDecorator(VariableBiomassBasedBiology delegate) {
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


    @Override
    public Double getCarryingCapacity(Species species) {
        return delegate.getCarryingCapacity(species);
    }

    @Override
    public Double getCarryingCapacity(int index) {
        return delegate.getCarryingCapacity(index);
    }

    @Override
    public void setCarryingCapacity(Species s, double newCarryingCapacity) {
        delegate.setCarryingCapacity(s, newCarryingCapacity);
    }

    @Override
    public void setCurrentBiomass(Species s, double newCurrentBiomass) {
        delegate.setCurrentBiomass(s, newCurrentBiomass);
    }

    @Override
    public Double[] getCurrentBiomass() {
        return delegate.getCurrentBiomass();
    }
}
