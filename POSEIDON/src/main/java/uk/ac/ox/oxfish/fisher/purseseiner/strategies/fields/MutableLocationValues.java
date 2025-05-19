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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.model.StepOrder.POLICY_UPDATE;

public abstract class MutableLocationValues<A>
    extends PurseSeinerActionObserver<A>
    implements LocationValues, Steppable {

    private static final long serialVersionUID = 8981125814239200579L;
    private final Observers observers = new Observers();
    private final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader;
    private final double decayRate;
    private Map<Int2D, Double> values;

    MutableLocationValues(
        final Class<A> observedClass,
        final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader,
        final double decayRate
    ) {
        super(observedClass);
        this.valueLoader = valueLoader;
        this.decayRate = decayRate;
    }

    public Observers getObservers() {
        return observers;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
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
        return values == null ? null : values.entrySet();
    }

    @Override
    public void observe(final A observable) {
        observeValue(observable).ifPresent(entry ->
            values.merge(entry.getKey(), entry.getValue(), Double::sum)
        );
        // Notify our observers of the change
        observers.reactTo(this);
    }

    abstract Optional<Entry<Int2D, Double>> observeValue(final A observable);

    @Override
    public void step(final SimState simState) {
        // apply exponential decay
        values.replaceAll((location, value) -> value * (1 - decayRate));
    }

    void removeIf(final Predicate<? super Entry<Int2D, Double>> predicate) {
        values.entrySet().removeIf(predicate);
    }

}
