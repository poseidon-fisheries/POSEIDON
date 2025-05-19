/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HabitatAwareRandomCatchability;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Creates HabitatAwareRandomCatchability gears
 * Created by carrknight on 9/30/15.
 */
public class HabitatAwareGearFactory implements AlgorithmFactory<HabitatAwareRandomCatchability> {


    private DoubleParameter meanCatchabilityRocky = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilityRocky = new FixedDoubleParameter(0);


    private DoubleParameter meanCatchabilitySandy = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilitySandy = new FixedDoubleParameter(0);

    private DoubleParameter trawlSpeed = new FixedDoubleParameter(5);


    public HabitatAwareGearFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HabitatAwareRandomCatchability apply(final FishState state) {


        final int species = state.getSpecies().size();
        final double[] meansRocky = new double[species];
        final double[] meansSandy = new double[species];
        final double[] stdRocky = new double[species];
        final double[] stdSandy = new double[species];


        for (int i = 0; i < meansSandy.length; i++) {
            meansRocky[i] = meanCatchabilityRocky.applyAsDouble(state.getRandom());
            meansSandy[i] = meanCatchabilitySandy.applyAsDouble(state.getRandom());
            stdSandy[i] = standardDeviationCatchabilitySandy.applyAsDouble(state.getRandom());
            stdRocky[i] = standardDeviationCatchabilityRocky.applyAsDouble(state.getRandom());
        }

        return new HabitatAwareRandomCatchability(meansSandy, stdSandy, meansRocky, stdRocky,
            trawlSpeed.applyAsDouble(state.getRandom())
        );

    }

    public DoubleParameter getMeanCatchabilityRocky() {
        return meanCatchabilityRocky;
    }

    public void setMeanCatchabilityRocky(final DoubleParameter meanCatchabilityRocky) {
        this.meanCatchabilityRocky = meanCatchabilityRocky;
    }

    public DoubleParameter getStandardDeviationCatchabilityRocky() {
        return standardDeviationCatchabilityRocky;
    }

    public void setStandardDeviationCatchabilityRocky(
        final DoubleParameter standardDeviationCatchabilityRocky
    ) {
        this.standardDeviationCatchabilityRocky = standardDeviationCatchabilityRocky;
    }

    public DoubleParameter getMeanCatchabilitySandy() {
        return meanCatchabilitySandy;
    }

    public void setMeanCatchabilitySandy(final DoubleParameter meanCatchabilitySandy) {
        this.meanCatchabilitySandy = meanCatchabilitySandy;
    }

    public DoubleParameter getStandardDeviationCatchabilitySandy() {
        return standardDeviationCatchabilitySandy;
    }

    public void setStandardDeviationCatchabilitySandy(
        final DoubleParameter standardDeviationCatchabilitySandy
    ) {
        this.standardDeviationCatchabilitySandy = standardDeviationCatchabilitySandy;
    }

    public DoubleParameter getTrawlSpeed() {
        return trawlSpeed;
    }

    public void setTrawlSpeed(final DoubleParameter trawlSpeed) {
        this.trawlSpeed = trawlSpeed;
    }
}
