/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PeriodHabitContinuousExtractor;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

/**
 * Created by carrknight on 7/17/17.
 */
public class BarebonesContinuousDestinationFactory extends BarebonesLogitDestinationFactory {

    @NotNull
    @Override
    public ObservationExtractor buildHabitExtractor(
            MapDiscretization discretization, int period) {
        return new PeriodHabitContinuousExtractor(discretization,period);

    }
}
