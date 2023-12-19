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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.ReliableFishValueCalculator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class SetLocationValues<A extends AbstractSetAction>
    extends MutableLocationValues<A> {

    private static final long serialVersionUID = -6469174194098636736L;

    SetLocationValues(
        final Class<A> observedClass,
        final Function<? super Fisher, ? extends Map<Int2D, Double>> loadValues,
        final double decayRate
    ) {
        super(observedClass, loadValues, decayRate);
    }

    @Override
    Optional<Entry<Int2D, Double>> observeValue(final A setAction) {
        final Fisher fisher = setAction.getFisher();
        final Int2D gridLocation = fisher.getLocation().getGridLocation();
        final FishValueCalculator fishValueCalculator =
            new ReliableFishValueCalculator(fisher.grabState().getBiology());
        final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
        return setAction.getCatchesKept()
            .map(catchesKept -> fishValueCalculator.valueOf(catchesKept, prices))
            .map(valueOfCatch -> entry(gridLocation, valueOfCatch));
    }

}
