/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LastMomentAbundanceFadWithRange;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;

public class LastMomentAbundanceFadWithRangeInitializer<B extends LocalBiology>
    implements FadInitializer<AbundanceLocalBiology, LastMomentAbundanceFadWithRange> {

    private final int daysItTakeToFillUp;
    private final int daysInWaterBeforeAttraction;

    final private Map<Species, NonMutatingArrayFilter> selectivityFilters;
    private final double dudProbability;

    private final double[] maxCatchabilityPerSpecies;

    private final GlobalBiology biology;

    private final int rangeInSeatiles;
    private final Map<Species, Double> fishReleaseProbabilities;

    public LastMomentAbundanceFadWithRangeInitializer(
        final int daysItTakeToFillUp,
        final int daysInWaterBeforeAttraction,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters,
        final double dudProbability,
        final double[] maxCatchabilityPerSpecies,
        final GlobalBiology biology,
        final int rangeInSeatiles,
        final Map<Species, Double> fishReleaseProbabilities
    ) {
        this.daysItTakeToFillUp = daysItTakeToFillUp;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.selectivityFilters = selectivityFilters;
        this.dudProbability = dudProbability;
        this.maxCatchabilityPerSpecies = maxCatchabilityPerSpecies;
        this.biology = biology;
        this.rangeInSeatiles = rangeInSeatiles;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    @Override
    public LastMomentAbundanceFadWithRange makeFad(
        final FadManager fadManager,
        final Fisher owner,
        final SeaTile initialLocation,
        final MersenneTwisterFast rng
    ) {
        return new LastMomentAbundanceFadWithRange(
            owner != null ? owner.getCurrentTrip() : null,
            owner.grabState().getStep(),
            initialLocation.getGridLocation(),
            fadManager,
            daysItTakeToFillUp,
            daysInWaterBeforeAttraction,
            maxCatchabilityPerSpecies,
            rng.nextBoolean(dudProbability),
            rangeInSeatiles,
            selectivityFilters,
            biology,
            fishReleaseProbabilities
        );
    }

}
