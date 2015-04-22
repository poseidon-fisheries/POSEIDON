package uk.ac.ox.oxfish.biology;

/**
 * This local biology has the same fixed biomass for each specie . It never gets ruined by fishing and is the same for any specie
 * Created by carrknight on 4/11/15.
 */
public class ConstantLocalBiology implements LocalBiology {

    final private Double fixedBiomass;

    public ConstantLocalBiology(double fixedBiomass) {
        this.fixedBiomass = fixedBiomass;
    }

    /**
     * returned the fixed biomass
     */
    @Override
    public Double getBiomass(Specie specie) {
        return fixedBiomass;
    }

    /**
     * nothing happens
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished)
    {

    }
}
