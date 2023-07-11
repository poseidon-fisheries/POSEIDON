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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing;

import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartIfAnyActionPermitted;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedRestTimeDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class PurseSeinerDepartingStrategyFactory
    implements AlgorithmFactory<CompositeDepartingStrategy> {

    private boolean destinationBased;

    public PurseSeinerDepartingStrategyFactory() {
        destinationBased = true;

    }

    public PurseSeinerDepartingStrategyFactory(final boolean destinationBased) {
        this.destinationBased = destinationBased;
    }

    @Override
    public CompositeDepartingStrategy apply(final FishState state) {
        if (destinationBased)
            return new CompositeDepartingStrategy(
                new FixedRestTimeDepartingStrategy(0),
                // rest times assigned in TunaScenario.populateModel
                new DepartIfAnyActionPermitted(),
                new DestinationBasedDepartingStrategy()
            );
        else {
            return new CompositeDepartingStrategy(
                new FixedRestTimeDepartingStrategy(0),
                // rest times assigned in TunaScenario.populateModel
                new DepartIfAnyActionPermitted()
            );
        }
    }

    public boolean isDestinationBased() {
        return destinationBased;
    }

    public void setDestinationBased(final boolean destinationBased) {
        this.destinationBased = destinationBased;
    }
}
