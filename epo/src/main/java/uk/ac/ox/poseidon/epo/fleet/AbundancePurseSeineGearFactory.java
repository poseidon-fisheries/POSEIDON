/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.epo.fleet;

import uk.ac.ox.oxfish.fisher.purseseiner.equipment.AbundancePurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.ParameterTable;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class AbundancePurseSeineGearFactory extends PurseSeineGearFactory {

    public AbundancePurseSeineGearFactory(
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializerFactory,
        final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculatorFactory,
        final AlgorithmFactory<? extends ParameterTable> otherParameters
    ) {
        super(targetYear, fadInitializerFactory, fishValueCalculatorFactory, otherParameters);
    }

    @SuppressWarnings("unused")
    public AbundancePurseSeineGearFactory() {
    }

    @Override
    protected PurseSeineGear makeGear(
        final FadManager fadManager,
        final double successfulSetProbability,
        final double maxAllowableShear
    ) {
        return new AbundancePurseSeineGear(
            fadManager,
            successfulSetProbability,
            maxAllowableShear
        );
    }

}
