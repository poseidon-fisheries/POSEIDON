package uk.ac.ox.oxfish.fisher.equipment.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getMarkets;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.priceOfFishHere;

public class Fad {

    public static final double BUOY_VALUE = 1000.0; // Buoy value in dollars, TODO: should be a parameter

    private final FadManager owner;
    private final BiomassLocalBiology biology;
    final private double[] attractionRates; // proportion of underlying biomass attracted per day
    final private double fishReleaseProbability; // daily probability of releasing fish from the FAD

    public Fad(
        FadManager owner,
        BiomassLocalBiology biology,
        double[] attractionRates,
        double fishReleaseProbability
    ) {
        this.owner = owner;
        this.biology = biology;
        this.attractionRates = attractionRates;
        this.fishReleaseProbability = fishReleaseProbability;
    }

    /* For now, just aggregate fish in fixed proportion of the underlying biomass.
       We'll probably need different types of FADs in the future when we start
       complexifying the model.
    */
    public void aggregateFish(VariableBiomassBasedBiology seaTileBiology, GlobalBiology globalBiology) {
        // Calculate the catches and add them to the FAD biology:
        double[] catches = new double[globalBiology.getSize()];
        for (Species species : globalBiology.getSpecies()) {
            double currentFadBiomass = biology.getBiomass(species);
            double maxFadBiomass = biology.getCarryingCapacity(species);
            double maxCatch = max(0, maxFadBiomass - currentFadBiomass);
            double caught = min(seaTileBiology.getBiomass(species) * attractionRates[species.getIndex()], maxCatch);
            biology.setCurrentBiomass(species, min(currentFadBiomass + caught, maxFadBiomass));
            catches[species.getIndex()] = caught;
        }
        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(catches);
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(catchObject, catchObject, globalBiology);
    }

    public void maybeReleaseFish(Iterable<Species> allSpecies, LocalBiology seaTileBiology, MersenneTwisterFast rng) {
        if (rng.nextDouble() < fishReleaseProbability) releaseFish(allSpecies, seaTileBiology);
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. If the local biology is not
     * biomass based (most likely because we're outside the habitable zone), the fish is lost.
     */
    public void releaseFish(Iterable<Species> allSpecies, LocalBiology seaTileBiology) {
        if (seaTileBiology instanceof VariableBiomassBasedBiology)
            releaseFish(allSpecies, (VariableBiomassBasedBiology) seaTileBiology);
        else
            releaseFish(allSpecies);
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology.
     * In the unlikely event that the sea tile's carrying capacity is exceeded, the extra fish is lost.
     */
    public void releaseFish(Iterable<Species> allSpecies, VariableBiomassBasedBiology seaTileBiology) {
        allSpecies.forEach(species -> {
            final double seaTileBiomass = seaTileBiology.getBiomass(species);
            final double fadBiomass = biology.getBiomass(species);
            final double availableSeaTileCapacity = seaTileBiology.getCarryingCapacity(species) - seaTileBiomass;
            final double biomassToTransfer = min(availableSeaTileCapacity, fadBiomass);
            biology.setCurrentBiomass(species, fadBiomass - biomassToTransfer);
            seaTileBiology.setCurrentBiomass(species, seaTileBiomass + biomassToTransfer);
        });
        // release whatever is left in the FAD if the sea tile could not absorb it
        releaseFish(allSpecies);
    }

    public void maybeReleaseFish(Iterable<Species> allSpecies, MersenneTwisterFast rng) {
        if (rng.nextDouble() < fishReleaseProbability) releaseFish(allSpecies);
    }

    /**
     * Remove biomass for all the given species from the FAD without sending it anywhere, therefore losing the fish.
     */
    public void releaseFish(Iterable<Species> allSpecies) {
        allSpecies.forEach(species -> {
            getOwner().getFisher().getYearlyCounter().count(biomassLostCounterName(species), biology.getBiomass(species));
            biology.setCurrentBiomass(species, 0);
        });
    }

    public FadManager getOwner() { return owner; }

    public static String biomassLostCounterName(Species species) { return biomassLostCounterName(species.getName()); }

    public static String biomassLostCounterName(String speciesName) { return speciesName + " biomass lost (kg)"; }

    public double valueOfSet(Fisher fisher) {
        double buoyValue = getOwner() == getFadManager(fisher) ? BUOY_VALUE : 0;
        return buoyValue + priceOfFishHere(getBiology(), getMarkets(fisher));
    }

    public BiomassLocalBiology getBiology() { return biology; }

}
