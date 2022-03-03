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

import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

/**
 * Inspired somewhat by the IOTC paper: a FAD that "activates" after a certain number of days and then attracts at a fixed rate until full.
 * Whether anything gets attracted depends on there being enough local biomass (over a threshold)
 */
public class AbundanceLinearIntervalAttractor implements FishAttractor<AbundanceLocalBiology, AbundanceFad> {


    private final int daysInWaterBeforeAttraction;

    private final int daysItTakesToFillUp;

    final double[] carryingCapacitiesPerSpecies;

    final double[] dailyBiomassAttractedPerSpecies;

    final double minBiomassToActivate;

    public AbundanceLinearIntervalAttractor(
            int daysInWaterBeforeAttraction, int daysItTakesToFillUp, double[] carryingCapacitiesPerSpecies,
            double[] dailyBiomassAttractedPerSpecies, double minBiomassToActivate) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.daysItTakesToFillUp = daysItTakesToFillUp;
        this.carryingCapacitiesPerSpecies = carryingCapacitiesPerSpecies;
        this.dailyBiomassAttractedPerSpecies = dailyBiomassAttractedPerSpecies;
        this.minBiomassToActivate = minBiomassToActivate;
        throw new RuntimeException("not finished yet");

    }

    @Nullable
    @Override
    public WeightedObject<AbundanceLocalBiology> attractImplementation(
            AbundanceLocalBiology seaTileBiology, AbundanceFad fad) {

        //attract nothing before spending enough steps in
        if(fad.getStepDeployed()<daysInWaterBeforeAttraction)
                return null;
        //start weighing stuff
        double[] currentBiomass = fad.getBiology().getCurrentBiomass();
        //don't bother attracting if full
        //if(currentBiomass[i]>)

        //don't bother attracting if there is less biomass than what is needed to attract a single day

        //don't bother attracting below threshold

        //attract

        throw new RuntimeException("not finished yet");
    }
}
