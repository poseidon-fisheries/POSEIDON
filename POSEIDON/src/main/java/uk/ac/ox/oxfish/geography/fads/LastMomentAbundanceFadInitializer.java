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

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LastMomentAbundanceFad;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;

public class LastMomentAbundanceFadInitializer implements FadInitializer<AbundanceLocalBiology, LastMomentAbundanceFad> {

    private final int daysItTakeToFillUp;
    private final int daysInWaterBeforeAttraction;

    final private Map<Species, NonMutatingArrayFilter> selectivityFilters;
    private final double dudProbability;

    private final double[] maxCatchabilityPerSpecies;

    private final GlobalBiology biology;

    public LastMomentAbundanceFadInitializer(
        final int daysItTakeToFillUp, final int daysInWaterBeforeAttraction,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters, final double dudProbability,
        final double[] maxCatchabilityPerSpecies,
        final GlobalBiology biology
    ) {
        this.daysItTakeToFillUp = daysItTakeToFillUp;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.selectivityFilters = selectivityFilters;
        this.dudProbability = dudProbability;
        this.maxCatchabilityPerSpecies = maxCatchabilityPerSpecies;
        this.biology = biology;
    }

    @Override
    public LastMomentAbundanceFad makeFad(
        final FadManager<AbundanceLocalBiology, LastMomentAbundanceFad> fadManager,
        final Fisher owner,
        final SeaTile initialLocation,
        final MersenneTwisterFast rng
    ) {
        return new LastMomentAbundanceFad(
            owner != null ? owner.getCurrentTrip() : null,
            owner.grabState().getStep(),
            initialLocation.getGridLocation(),
            0d,
            fadManager,
            daysItTakeToFillUp,
            daysInWaterBeforeAttraction,
            rng.nextBoolean(dudProbability),
            maxCatchabilityPerSpecies,
            selectivityFilters,
            biology
        );

    }

}
