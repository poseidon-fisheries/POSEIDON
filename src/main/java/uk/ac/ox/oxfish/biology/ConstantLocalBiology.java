package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This local biology has the same fixed biomass for each specie . It never gets ruined by fishing and is the same for any specie.
 * It doesn't represent realistically the number of fish present, rather it just divides the biomass by weight of fish
 * at age 0 and assumes there are that many fish at age 0
 * Created by carrknight on 4/11/15.
 */
public class ConstantLocalBiology extends AbstractBiomassBasedBiology {

    final private Double fixedBiomass;

    public ConstantLocalBiology(double fixedBiomass) {
        this.fixedBiomass = fixedBiomass;
    }

    /**
     * returned the fixed biomass
     */
    @Override
    public Double getBiomass(Species species) {
        return fixedBiomass;
    }

    /**
     * nothing happens
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology)
    {

    }

    @Override
    public String toString() {
        return "fixed at " + fixedBiomass;
    }


    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }


}
