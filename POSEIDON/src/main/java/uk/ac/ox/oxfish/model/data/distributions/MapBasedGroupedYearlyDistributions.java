package uk.ac.ox.oxfish.model.data.distributions;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class MapBasedGroupedYearlyDistributions implements GroupedYearlyDistributions {

    private final Map<String, ? extends Map<Integer, double[]>> map;

    MapBasedGroupedYearlyDistributions(
        final Map<String, ? extends Map<Integer, double[]>> map
    ) {
        this.map =
            map.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> ImmutableMap.copyOf(entry.getValue())
            ));
    }

    @Override
    public Map<Integer, double[]> apply(final String group) {
        return checkNotNull(map.get(group));
    }
}
