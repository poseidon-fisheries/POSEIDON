package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;

public class MaximumOfFiltersTest {


    @Test
    public void maximumWorksWell() {

        Species species = new Species(
            "test",
            new FromListMeristics(
                new double[]{10, 20, 30}, 2
            )
        );
        ArrayFilter firstFilter = new ArrayFilter(false,
            new double[]{0.5, 0, 0.5}, new double[]{0.2, 0, 0.2}
        );

        ArrayFilter secondFilter = new ArrayFilter(false,
            new double[]{0, 0.5, 0}, new double[]{1, 1, 1}
        );

        MaximumOfFilters filters = new MaximumOfFilters(
            firstFilter, secondFilter
        );

        double[][] abundance = new double[2][3];
        for (int subdivision = 0; subdivision < 2; subdivision++) {
            for (int bin = 0; bin < 3; bin++) {
                abundance[subdivision][bin] = 1;
            }
        }

        final double[][] filtered = filters.filter(species, abundance);
        Assert.assertArrayEquals(filtered[0], new double[]{0.5, 0.5, 0.5}, .001);
        Assert.assertArrayEquals(filtered[1], new double[]{1, 1, 1}, .001);

    }
}