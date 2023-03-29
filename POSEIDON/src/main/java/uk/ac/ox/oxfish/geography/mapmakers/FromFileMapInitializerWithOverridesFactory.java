package uk.ac.ox.oxfish.geography.mapmakers;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

public class FromFileMapInitializerWithOverridesFactory extends FromFileMapInitializerFactory {


    /**
     * a list of x,y,depth (GRID not latlong) to override the altitude maps...
     */
    private List<String> depthOverrides = Lists.newArrayList("0,0,100");

    public FromFileMapInitializerWithOverridesFactory() {
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromFileMapInitializer apply(final FishState state) {
        final MersenneTwisterFast rng = state.getRandom();
        final HashBasedTable<Integer, Integer, Double> overrides = HashBasedTable.create(
            depthOverrides.size(),
            depthOverrides.size()
        );
        for (final String override : depthOverrides) {
            final String[] split = override.split(",");
            overrides.put(
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1]),
                Double.parseDouble(split[2])
            );

        }

        return new FromFileMapInitializer(
            getMapFile().get(),
            (int) getGridWidthInCell().applyAsDouble(rng),
            getMapPaddingInDegrees().applyAsDouble(rng),
            isHeader(),
            isLatLong(),
            overrides
        );
    }

    public List<String> getDepthOverrides() {
        return depthOverrides;
    }

    public void setDepthOverrides(final List<String> depthOverrides) {
        this.depthOverrides = depthOverrides;
    }
}
