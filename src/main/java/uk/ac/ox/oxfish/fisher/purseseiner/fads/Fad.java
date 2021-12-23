/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Arrays;
import java.util.Map;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;

/**
 * A fish aggregation device.
 *
 * @param <B> The type of local biology that is used by the FAD to aggreate fish.
 * @param <F> The type of the subclass extending this class. The declaration for this, {@code F
 *            extends Fad<B, F>}, gives us a "self recursive type". It allows us to ask for a {@code
 *            FadManager<B, F>}, where {@code F} is the actual subtype and not just a {@code
 *            Fad<B>}, which would lead to all sorts of trouble down the line.
 */
public abstract class Fad<B extends LocalBiology, F extends Fad<B, F>> implements Locatable {

    private final FadManager<B, F> owner;
    private final TripRecord tripDeployed;
    private final B biology;
    private final Map<Species, FishBiomassAttractor> fadBiomassAttractors;
    private final double fishReleaseProbability;
    private final int stepDeployed;
    private final Int2D locationDeployed;
    private final double totalCarryingCapacity;

    public Fad(
        final FadManager<B, F> owner,
        final B biology,
        final Map<Species, FishBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed,
        final double totalCarryingCapacity
    ) {
        this.owner = owner;
        this.tripDeployed = owner.getFisher().getCurrentTrip();
        this.biology = biology;
        this.fadBiomassAttractors = ImmutableMap.copyOf(fadBiomassAttractors);
        this.fishReleaseProbability = fishReleaseProbability;
        this.stepDeployed = stepDeployed;
        this.locationDeployed = locationDeployed;
        this.totalCarryingCapacity = totalCarryingCapacity;
    }

    public TripRecord getTripDeployed() {
        return tripDeployed;
    }

    public int getStepDeployed() {
        return stepDeployed;
    }

    public Int2D getLocationDeployed() {
        return locationDeployed;
    }

    public void maybeReleaseFish(
        final Iterable<Species> allSpecies,
        final LocalBiology seaTileBiology,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies, seaTileBiology);
        }
    }

    public abstract void releaseFish(Iterable<Species> allSpecies, LocalBiology seaTileBiology);

    public void maybeReleaseFish(
        final Iterable<Species> allSpecies,
        final MersenneTwisterFast rng
    ) {
        if (rng.nextDouble() < fishReleaseProbability) {
            releaseFish(allSpecies);
        }
    }

    public abstract void releaseFish(final Iterable<Species> allSpecies);

    public SeaTile getLocation() {
        return getOwner().getFadMap()
            .getFadTile(this)
            .orElseThrow(() -> new RuntimeException(this + " not on map!"));
    }

    public FadManager<B, F> getOwner() {
        return owner;
    }

    public double valueOfFishFor(final Fisher fisher) {
        return new FishValueCalculator(fisher).valueOf(getBiology());
    }

    public B getBiology() {
        return biology;
    }

    public void aggregateFish(
        final B seaTileBiology,
        final GlobalBiology globalBiology
    ) {
        // Calculate the catches
        final double[] catches = computeCatches(seaTileBiology);
        // add them to the FAD biology
        final Catch catchObject = addCatchesToFad(seaTileBiology, globalBiology, catches);
        // and remove the catches from the underlying biology:
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(
            catchObject,
            catchObject,
            globalBiology
        );
    }

    /**
     * This uses the biomass attractors to compute the biomass array of catches. It takes the FAD
     * carrying capacity into account so no more biomass than what the FAD can carry will be
     * attracted. Note that this method is also used for {@link AbundanceFad} objects, where the
     * biomass catch will later be converted to age-structured catches by the {@link
     * #addCatchesToFad(LocalBiology, GlobalBiology, double[])} method.
     */
    private double[] computeCatches(final B seaTileBiology) {
        // Calculate the catches and add them to the FAD biology:
        final double[] tileBiomass = getBiomass(seaTileBiology);
        final double[] fadBiomass = getBiomass(getBiology());
        final double[] catches = new double[tileBiomass.length];
        final double totalFadBiomass = Arrays.stream(fadBiomass).sum();

        getFadBiomassAttractors().forEach((species, fadBiomassAttractor) -> {
            final int i = species.getIndex();
            if (fadBiomassAttractor.shouldAttract(tileBiomass[i], totalFadBiomass)) {
                catches[i] = fadBiomassAttractor.biomassAttracted(
                    tileBiomass[i],
                    fadBiomass[i],
                    totalFadBiomass
                );
            }
        });

        // Make sure that the catches don't exceed total FAD capacity
        final double totalCaughtBiomass = Arrays.stream(catches).sum();
        final double fadCapacity = Math.max(
            0, // to account for floating point errors where biomass on the FAD is a iota too high
            getTotalCarryingCapacity() - Arrays.stream(fadBiomass).sum()
        );
        if (totalCaughtBiomass > fadCapacity) {
            final double catchRatio = fadCapacity / totalCaughtBiomass;
            for (int i = 0; i < catches.length; i++) {
                catches[i] *= catchRatio;
            }
        }

        return catches;
    }

    public Map<Species, FishBiomassAttractor> getFadBiomassAttractors() {
        return fadBiomassAttractors;
    }

    /* This needs different implementations in the subclasses because {@link LocalBiology}
     * doesn't have a {@code getBiomass()} method even if both {@link BiomassLocalBiology}
     * and {@link AbundanceLocalBiology} happen to have one.
     */
    abstract double[] getBiomass(B biology);

    public double getTotalCarryingCapacity() {
        return totalCarryingCapacity;
    }

    protected abstract Catch addCatchesToFad(
        B seaTileBiology,
        GlobalBiology globalBiology,
        double[] catches
    );
}
