/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.data.monitors.accumulators;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.IterativeAverage;

import java.util.function.DoubleSupplier;

/**
 * Accumulates the proportion of true values observed.
 */
public class ProportionAccumulator implements Accumulator<Boolean>, DoubleSupplier {

    private final Averager<Double> averager = new IterativeAverage<>();

    @Override
    public String getNameFormat() {
        return "Proportion of %s";
    }

    @Override
    public void accumulate(final Boolean value) {
        averager.addObservation(value ? 1.0 : 0.0);
    }

    @Override
    public double applyAsDouble(final FishState fishState) {
        return getAsDouble();
    }

    @Override
    public double getAsDouble() {
        return averager.getSmoothedObservation();
    }
}
