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

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

public abstract class MutableLocationValues<A>
    extends PurseSeinerActionObserver<A>
    implements LocationValues, Steppable {

    private static final int MAXIMUM_NUMBER_OF_VALUES = 50;

    private final Function<Fisher, Map<Int2D, Double>> valueLoader;
    private final double decayRate;
    private Map<Int2D, Double> values;

    MutableLocationValues(
        final Class<A> observedClass,
        final Function<Fisher, Map<Int2D, Double>> valueLoader,
        final double decayRate
    ) {
        super(observedClass);
        this.valueLoader = valueLoader;
        this.decayRate = decayRate;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.values = new HashMap<>(valueLoader.apply(fisher));
        getFadManager(fisher).registerObserver(getObservedClass(), this);
        model.scheduleEveryYear(this, POLICY_UPDATE);
    }

    @Override
    public double getValueAt(final Int2D location) {
        return values.getOrDefault(location, 0.0);
    }

    @Override
    public Set<Entry<Int2D, Double>> getValues() {
        return values.entrySet();
    }

    @Override
    public void observe(final A observable) {
        observeValue(observable).ifPresent(entry ->
            values.merge(entry.getKey(), entry.getValue(), Double::sum)
        );
    }

    abstract Optional<Entry<Int2D, Double>> observeValue(final A observable);

    @Override
    public void step(final SimState simState) {

        if (values.size() > MAXIMUM_NUMBER_OF_VALUES) {
            // when reaching the limit, forget all the values that are below average
            values.values().stream().mapToDouble(Double::doubleValue).average().ifPresent(average ->
                values.entrySet().removeIf(entry -> entry.getValue() < average)
            );
        }

        // apply exponential decay
        values.replaceAll((location, value) -> value * (1 - decayRate));
    }

}
