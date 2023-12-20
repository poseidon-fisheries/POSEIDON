package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class FixedLocationValues extends ObservableLocationValues {

    private final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader;
    // nothing to observe when values are fixed, but required by the LocationValues interface
    private Map<Int2D, Double> values;

    FixedLocationValues(final Function<? super Fisher, ? extends Map<Int2D, Double>> valueLoader) {
        this.valueLoader = valueLoader;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        this.values = ImmutableMap.copyOf(valueLoader.apply(fisher));
    }

    @Override
    public double getValueAt(final Int2D location) {
        return checkNotNull(values).getOrDefault(location, 0.0);
    }

    @Override
    public Set<Map.Entry<Int2D, Double>> getValues() {
        return values.entrySet();
    }

}
