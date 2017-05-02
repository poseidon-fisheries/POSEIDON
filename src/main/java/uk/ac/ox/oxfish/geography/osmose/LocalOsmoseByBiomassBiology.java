package uk.ac.ox.oxfish.geography.osmose;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.ouce.oxfish.ExogenousMortality;
import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.oxfish.biology.AbstractBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The local biology object that links up with the OSMOSE cell. In reality delegates everything to the other local osmose
 * class by assuming that recruitment age is at 0
 * Created by carrknight on 6/25/15.
 */
public class LocalOsmoseByBiomassBiology extends AbstractBiomassBasedBiology
{

    private final LocalOsmoseWithoutRecruitmentBiology delegate;



    public LocalOsmoseByBiomassBiology(
            ExogenousMortality mortality, CellBiomass counter,
            int numberOfSpecies, MersenneTwisterFast random,
            double scalingFactor)
    {

        delegate = new LocalOsmoseWithoutRecruitmentBiology(
                mortality,
                counter,
                random,
                scalingFactor,
                new int[numberOfSpecies]
        );

    }


    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        return delegate.getBiomass(species);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {
        delegate.reactToThisAmountOfBiomassBeingFished(caught, notDiscarded,biology );
    }

    /**
     * ignored
     * @param model
     */
    @Override
    public void start(FishState model) {
        delegate.start(model);
    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
    }

    public void osmoseStep() {
        delegate.osmoseStep();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
