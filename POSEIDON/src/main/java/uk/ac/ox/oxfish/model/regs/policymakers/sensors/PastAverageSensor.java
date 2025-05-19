/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.function.Function;

/**
 * reads column name, returns the average of its last X observations
 */
public class PastAverageSensor implements Sensor<FishState, Double> {


    private static final long serialVersionUID = 3782345834340742854L;
    private final UnchangingPastSensor delegate;


    public PastAverageSensor(final String indicatorColumnName, final int yearsToLookBack) {

        delegate = new UnchangingPastSensor(
            indicatorColumnName,
            1d,
            yearsToLookBack
        );

    }


    @Override
    public Double scan(final FishState system) {
        final Double scan = delegate.scan(system);
        delegate.setTargetSet(Double.NaN);
        return scan;

    }

    public String getIndicatorColumnName() {
        return delegate.getIndicatorColumnName();
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return delegate.getIndicatorTransformer();
    }

    public void setIndicatorTransformer(final Function<Double, Double> indicatorTransformer) {
        delegate.setIndicatorTransformer(indicatorTransformer);
    }

    public double getIndicatorMultiplier() {
        return delegate.getIndicatorMultiplier();
    }

    public int getYearsToLookBack() {
        return delegate.getYearsToLookBack();
    }

}
