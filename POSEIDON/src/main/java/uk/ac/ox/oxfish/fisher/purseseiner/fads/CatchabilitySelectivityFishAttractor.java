/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * simple "linear" attractor for abundance fads: as long as it is old enough (but not too old) the fad attracts a fixed
 * proportion (catchability) of the vulnerable population (i.e. the population that can be selected in a cell).
 * Each FAD also have a carrying capacity so that they cannot get any more full than a given amount
 */
public class CatchabilitySelectivityFishAttractor
    implements FishAttractor<
    AbundanceLocalBiology,
    PerSpeciesCarryingCapacity,
    AbundanceAggregatingFad<PerSpeciesCarryingCapacity>
    > {

    /**
     * given a fad, returns its current catchability per species
     */
    private final Function<Fad, double[]> catchabilityPerSpeciesSupplier;


    /**
     * as long as the FAD has been in the water less than this, it won't fill up
     */
    private final int daysInWaterBeforeAttraction;


    /**
     * if the fad has been attracting fish (potentially, anyway) for these many days, it stops attracting any more (but doesn't lose them, yet!)
     */
    private final int maximumAttractionDays;

    private final FishState model;

    private final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves;

    public CatchabilitySelectivityFishAttractor(
        final Function<Fad, double[]> catchabilityPerSpeciesSupplier,
        final int daysInWaterBeforeAttraction,
        final int maximumAttractionDays,
        final FishState model,
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves
    ) {
        this.catchabilityPerSpeciesSupplier = catchabilityPerSpeciesSupplier;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumAttractionDays = maximumAttractionDays;
        this.model = model;
        this.globalSelectivityCurves = globalSelectivityCurves;
    }

    public CatchabilitySelectivityFishAttractor(
        final DoubleParameter[] carryingCapacitiesGenerator,
        final double[] catchabilityPerSpecies,
        final int daysInWaterBeforeAttraction,
        final int maximumAttractionDays,
        final FishState model,
        final Map<Species, NonMutatingArrayFilter> globalSelectivityCurves
    ) {
        this.catchabilityPerSpeciesSupplier = abstractFad -> catchabilityPerSpecies;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumAttractionDays = maximumAttractionDays;
        this.model = model;
        this.globalSelectivityCurves = globalSelectivityCurves;
    }


    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
        final LocalBiology seaTileBiology,
        final AbundanceAggregatingFad<PerSpeciesCarryingCapacity> fad
    ) {
        //if it's too early or late don't bother
        if (
            model.getDay() - fad.getStepDeployed() / model.getStepsPerDay() < daysInWaterBeforeAttraction ||
                !fad.isActive() ||
                model.getDay() - fad.getStepDeployed() / model.getStepsPerDay() > daysInWaterBeforeAttraction + maximumAttractionDays
        )
            return null;
        final SeaTile location = fad.getLocation();
        final LocalBiology biology = location.getBiology();
        if (!(biology instanceof AbundanceLocalBiology))
            return null;

        final Map<Species, double[][]> abundanceHere = ((AbundanceLocalBiology) biology).getAbundance();
        final Map<Species, double[][]> caughtHere =
            abundanceHere.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> stream(entry.getValue())
                    .map(a -> new double[a.length])
                    .toArray(double[][]::new)
            ));
        //get the carrying capacities or generate them if they don't exist
        final double[] carryingCapacityHere = fad.getCarryingCapacity().getCarryingCapacities();
        for (final Species species : model.getSpecies()) {
            final NonMutatingArrayFilter selectivity = globalSelectivityCurves.get(species);

            //set up catches
            final double[][] abundanceInTile = abundanceHere.get(species);
            final double[][] abundanceCaught = caughtHere.get(species);
            caughtHere.put(species, abundanceCaught);

            //if you are full, ignore it!
            if (carryingCapacityHere[species.getIndex()] <= fad.getBiomass()[species.getIndex()])
                continue;

            //start filling them up!
            final double[] catchabilityHere = catchabilityPerSpeciesSupplier.apply(fad);
            for (int subdivision = 0; subdivision < abundanceInTile.length; subdivision++) {
                for (int bin = 0; bin < abundanceInTile[subdivision].length; bin++) {
                    abundanceCaught[subdivision][bin] = Math.max(
                        catchabilityHere[species.getIndex()] *
                            selectivity.getFilterValue(subdivision, bin) *
                            abundanceInTile[subdivision][bin],
                        0
                    );

                }
            }
        }
        final AbundanceLocalBiology toReturn = new AbundanceLocalBiology(caughtHere);
        return new WeightedObject<>(toReturn, toReturn.getTotalBiomass());


    }

}
