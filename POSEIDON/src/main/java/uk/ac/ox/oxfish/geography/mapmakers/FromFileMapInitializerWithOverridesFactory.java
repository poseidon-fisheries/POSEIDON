package uk.ac.ox.oxfish.geography.mapmakers;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.HashBasedTable;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Hashtable;
import java.util.List;

public class FromFileMapInitializerWithOverridesFactory extends FromFileMapInitializerFactory {


    /**
     * a list of x,y,depth (GRID not latlong) to override the altitude maps...
     */
    private List<String> depthOverrides = Lists.newArrayList("0,0,100");

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromFileMapInitializer apply(FishState state) {
        final MersenneTwisterFast rng = state.getRandom();
        final HashBasedTable<Integer, Integer, Double> overrides = HashBasedTable.create(depthOverrides.size(), depthOverrides.size());
        for (String override : depthOverrides) {
            final String[] split = override.split(",");
            overrides.put(
                    Integer.parseInt(split[0]),
                    Integer.parseInt(split[1]),
                    Double.parseDouble(split[2])
                    );

        }

        return new FromFileMapInitializer(
                getMapFile().get(),
                getGridWidthInCell().apply(rng).intValue(),
                getMapPaddingInDegrees().apply(rng),
                isHeader(),
                isLatLong(),
                overrides
        );
    }

    public FromFileMapInitializerWithOverridesFactory() {
    }

    public List<String> getDepthOverrides() {
        return depthOverrides;
    }

    public void setDepthOverrides(List<String> depthOverrides) {
        this.depthOverrides = depthOverrides;
    }
}
