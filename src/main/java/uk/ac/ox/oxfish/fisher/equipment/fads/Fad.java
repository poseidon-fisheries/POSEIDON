package uk.ac.ox.oxfish.fisher.equipment.fads;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.aggregation.FishAggregation;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;

public class Fad implements FishAggregation {

    private final FadManager owner;
    private final VariableBiomassBasedBiology aggregatedBiology;
    final private double proportionFished;

    public Fad(FadManager owner, VariableBiomassBasedBiology aggregatedBiology,
        double proportionFished) {
        this.owner = owner;
        this.aggregatedBiology = aggregatedBiology;
        this.proportionFished = proportionFished;
    }

    @Override
    public LocalBiology getAggregatedBiology() { return aggregatedBiology; }

    public FadManager getOwner() { return owner; }

    /* For now, just aggregate fish in fixed proportion of the underlying biomass.
       We'll probably need different types of FADs in the future when we start
       complexifying the model.
    */
    @Override
    public void aggregateFish(SeaTile seaTile, GlobalBiology globalBiology) {
        if (proportionFished > 0 && seaTile.isFishingEvenPossibleHere()) {
            // Calculate the catches and add them to the FAD biology:
            double[] catches = new double[globalBiology.getSize()];
            for (Species species : globalBiology.getSpecies()) {
                double currentBiomass = aggregatedBiology.getBiomass(species);
                double maxCatch = aggregatedBiology.getCarryingCapacity(species) - currentBiomass;
                double caught = Math.min(seaTile.getBiomass(species) * proportionFished, maxCatch);
                aggregatedBiology.setCurrentBiomass(species, currentBiomass + caught);
                catches[species.getIndex()] = caught;
            }
            // Remove the catches from the underlying biology:
            final Catch catchObject = new Catch(catches);
            seaTile.getBiology()
                .reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        }
    }

}
