package uk.ac.ox.oxfish.model.data.distributions;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class MapBasedEmpiricalDistributions implements EmpiricalDistributions {

    private final Map<Integer, ? extends Map<String, double[]>> map;

    public MapBasedEmpiricalDistributions(
        final Map<Integer, ? extends Map<String, double[]>> map
    ) {
        this.map =
            map.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> ImmutableMap.copyOf(entry.getValue())
            ));
    }

    @Override
    public double[] get(
        final int year,
        final String speciesCode
    ) {
        return checkNotNull(map.get(year)).get(speciesCode);
    }
}
