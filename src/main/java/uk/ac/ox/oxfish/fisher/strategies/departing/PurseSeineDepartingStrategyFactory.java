/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class PurseSeineDepartingStrategyFactory implements AlgorithmFactory<CompositeDepartingStrategy> {

    // source: https://github.com/poseidon-fisheries/tuna/commit/d1d0fce68ec9dc49aa353ec63a5d9a1fd7eee481
    private DoubleParameter hoursBetweenEachDeparture = new FixedDoubleParameter(340.3333);

    @Override public CompositeDepartingStrategy apply(FishState state) {
        return new CompositeDepartingStrategy(
            new FixedRestTimeDepartingStrategy(hoursBetweenEachDeparture.apply(state.getRandom())),
            new YearlyActionLimitsDepartingStrategy()
        );
    }

    @SuppressWarnings("unused") public DoubleParameter
    getHoursBetweenEachDeparture() { return hoursBetweenEachDeparture; }

    @SuppressWarnings("unused") public void
    setHoursBetweenEachDeparture(DoubleParameter hoursBetweenEachDeparture) {
        this.hoursBetweenEachDeparture = hoursBetweenEachDeparture;
    }

}
