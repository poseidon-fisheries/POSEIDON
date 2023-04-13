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

import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbundanceCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.Map;

public class LastMomentAbundanceFad extends LastMomentFad<AbundanceLocalBiology, LastMomentAbundanceFad> {

    private final Map<Species, NonMutatingArrayFilter> selectivityFilters;

    private final GlobalBiology biology;

    public LastMomentAbundanceFad(
        final TripRecord tripDeployed,
        final int stepDeployed,
        final Int2D locationDeployed,
        final double fishReleaseProbability,
        final FadManager<AbundanceLocalBiology> owner,
        final int daysItTakesToFillUp,
        final int daysInWaterBeforeAttraction,
        final boolean isDud,
        final double[] maxCatchabilityPerSpecies,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters,
        final GlobalBiology biology
    ) {
        super(
            tripDeployed,
            stepDeployed,
            locationDeployed,
            fishReleaseProbability,
            owner,
            daysItTakesToFillUp,
            daysInWaterBeforeAttraction,
            maxCatchabilityPerSpecies,
            isDud
        );
        this.selectivityFilters = selectivityFilters;

        this.biology = biology;
    }

    @Override
    public AbundanceLocalBiology getBiology() {
        final FishState state = super.getFishState();
        if (state == null)
            return new AbundanceLocalBiology(biology);
        final double[] catchability = getCurrentCatchabilityPerSpecies();
        if (catchability == null)
            return new AbundanceLocalBiology(biology);

        final LocalBiology biology = super.getLocation().getBiology();
        return extractFadBiologyFromLocalBiology(state, catchability, biology, selectivityFilters);
    }

    @NotNull
    public static AbundanceLocalBiology extractFadBiologyFromLocalBiology(
        final FishState state,
        final double[] catchability,
        final LocalBiology seaTileBiology,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        //for each species, same operation
        final Map<Species, double[][]> caughtAbundances = new HashMap<>();
        for (final Species species : state.getBiology().getSpecies()) {
            final StructuredAbundance localAbundance = seaTileBiology.getAbundance(species);
            final double[][] caughtAbundance = new double[localAbundance.getSubdivisions()][localAbundance.getBins()];
            caughtAbundances.put(species, caughtAbundance);
            final NonMutatingArrayFilter selectivity = selectivityFilters.get(species);
            for (int subdivision = 0; subdivision < localAbundance.getSubdivisions(); subdivision++) {
                for (int bins = 0; bins < localAbundance.getBins(); bins++) {
                    caughtAbundance[subdivision][bins] =
                        catchability[species.getIndex()] * localAbundance.getAbundance(subdivision, bins) *
                            selectivity.getFilterValue(subdivision, bins);

                    if (caughtAbundance[subdivision][bins] < .0001)
                        caughtAbundance[subdivision][bins] = 0;


                }
            }
        }

        return new AbundanceLocalBiology(caughtAbundances);
    }

    @Override
    protected CatchMaker<AbundanceLocalBiology> getCatchMaker() {
        return new AbundanceCatchMaker(biology);
    }
}
