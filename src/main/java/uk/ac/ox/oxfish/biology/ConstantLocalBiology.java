package uk.ac.ox.oxfish.biology;

/**
 * This local biology has a fixed biome. It never gets ruined by fishing and is the same for any specie
 * Created by carrknight on 4/11/15.
 */
public class ConstantLocalBiology implements LocalBiology {

    final private Integer fixedBiomass;

    public ConstantLocalBiology(int fixedBiomass) {
        this.fixedBiomass = fixedBiomass;
    }

    /**
     * returned the fixed biomass
     */
    @Override
    public Integer getBiomass(Specie specie) {
        return fixedBiomass;
    }

    /**
     * nothing happens
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Integer biomassFished)
    {

    }
}
