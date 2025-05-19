/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.epo.fleet;

import uk.ac.ox.oxfish.fisher.purseseiner.equipment.BiomassPurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.ParameterTable;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;
import uk.ac.ox.poseidon.geography.DoubleGrid;

public class BiomassPurseSeineGearFactory extends PurseSeineGearFactory {

    public BiomassPurseSeineGearFactory(
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializerFactory,
        final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculatorFactory,
        final AlgorithmFactory<? extends ParameterTable> otherParameters,
        final ComponentFactory<? extends TemporalMap<DoubleGrid>> shearGridFactory
    ) {
        super(targetYear, fadInitializerFactory, fishValueCalculatorFactory, otherParameters, shearGridFactory);
    }

    @SuppressWarnings("unused")
    public BiomassPurseSeineGearFactory() {
    }

    @Override
    protected PurseSeineGear makeGear(
        final FadManager fadManager,
        final double successfulSetProbability,
        final double maxAllowableShear,
        final TemporalMap<DoubleGrid> shearGrid
    ) {
        return new BiomassPurseSeineGear(
            fadManager,
            successfulSetProbability,
            maxAllowableShear,
            shearGrid
        );
    }
}
