package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;

public class SimplifiedDoubleNormalFilterTest {


    @Test
    public void doubleNormal() {

        final double[] length = new double[126];
        for (int i = 0; i <= 125; i++)
            length[i] = i;

        Species species = new Species(
            "test",
            new FromListMeristics(
                new double[126],
                length,
                1
            )
        );

        SimplifiedDoubleNormalFilter filter = new SimplifiedDoubleNormalFilter(true, false,
            30, 5, 10
        );

        final double[][] selex = filter.computeSelectivity(species);

        Assertions.assertEquals(selex[0][30], 1.0, .001);
        Assertions.assertEquals(selex[0][20], 6.250000e-02, .001);
        Assertions.assertEquals(selex[0][40], 0.5, .001);
        Assertions.assertEquals(selex[0][50], 6.250000e-02, .001);

    }
}