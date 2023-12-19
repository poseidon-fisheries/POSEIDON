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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class DeploymentLocationValues extends SetLocationValues<FadSetAction> {

    private static final long serialVersionUID = -7573058884700476680L;

    public DeploymentLocationValues(
        final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader,
        final double decayRate
    ) {
        super(FadSetAction.class, valueLoader, decayRate);
    }

    @Override
    Optional<Entry<Int2D, Double>> observeValue(
        final FadSetAction fadSetAction
    ) {
        // extract the value of the set but remap the location to the original deployment location
        return super
            .observeValue(fadSetAction)
            .map(e -> entry(fadSetAction.getFad().getLocationDeployed(), e.getValue()));
    }

}
