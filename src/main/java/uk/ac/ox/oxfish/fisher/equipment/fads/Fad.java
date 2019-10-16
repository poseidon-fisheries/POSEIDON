package uk.ac.ox.oxfish.fisher.equipment.fads;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.Optional;

import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

public class Fad {

    private final FadManager owner;
    private final BiomassLocalBiology biology;
    final private double attractionRate; // proportion of underlying biomass attracted per day

    public Fad(
        FadManager owner,
        BiomassLocalBiology biology,
        double attractionRate
    ) {
        this.owner = owner;
        this.biology = biology;
        this.attractionRate = attractionRate;
    }

    public BiomassLocalBiology getBiology() { return biology; }

    /* For now, just aggregate fish in fixed proportion of the underlying biomass.
       We'll probably need different types of FADs in the future when we start
       complexifying the model.
    */
    public void aggregateFish(VariableBiomassBasedBiology seaTileBiology, GlobalBiology globalBiology) {
        if (attractionRate > 0) {
            // Calculate the catches and add them to the FAD biology:
            double[] catches = new double[globalBiology.getSize()];
            for (Species species : globalBiology.getSpecies()) {
                double currentBiomass = biology.getBiomass(species);
                double maxBiomass = biology.getCarryingCapacity(species);
                double maxCatch = max(0, maxBiomass - currentBiomass);
                double caught = min(seaTileBiology.getBiomass(species) * attractionRate, maxCatch);
                biology.setCurrentBiomass(species, min(currentBiomass + caught, maxBiomass));
                catches[species.getIndex()] = caught;
            }
            // Remove the catches from the underlying biology:
            final Catch catchObject = new Catch(catches);
            seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
        }
    }

    public FadManager getOwner() { return owner; }

    public void releaseFish(LocalBiology localBiology, GlobalBiology globalBiology) {
        Optional<VariableBiomassBasedBiology> variableBiomassBasedSeaTileBiology =
            localBiology instanceof VariableBiomassBasedBiology ?
                Optional.of((VariableBiomassBasedBiology) localBiology) :
                Optional.empty();
        for (Species species : globalBiology.getSpecies()) {
            // Remove biomass from the FAD...
            final double fadBiomass = biology.getBiomass(species);
            biology.setCurrentBiomass(species, 0);
            // ...and send that biomass down to the sea tile's biology.
            // If the local biology is not biomass based (most likely because
            // we're outside the habitable zone), the fish is lost.
            // In the unlikely event that the sea tile's carrying capacity is exceeded,
            // the extra fish is also lost.
            variableBiomassBasedSeaTileBiology.ifPresent(seaTileBiology -> {
                final double seaTileBiomass = seaTileBiology.getBiomass(species);
                final double seaTileCarryingCapacity = seaTileBiology.getCarryingCapacity(species);
                final double newSeaTileBiomass = min(seaTileBiomass + fadBiomass, seaTileCarryingCapacity);
                seaTileBiology.setCurrentBiomass(species, newSeaTileBiomass);
            });
        }
    }

    public double priceOfFishHere(Collection<Market> markets) {
        return markets.stream().mapToDouble(market ->
            getBiology().getBiomass(market.getSpecies()) * market.getMarginalPrice()
        ).sum();
    }


}
