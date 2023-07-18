package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AbundanceAggregationProcessTest {

    @Test
    public void AbundanceAggregationProcessTester() {
        //final BiologicalProcess<AbundanceLocalBiology> aggregationProcess = new AbundanceAggregationProcess();
        final AbundanceAggregator aggregator = new AbundanceAggregator();
        final Species species1 = new Species("Piano Tuna");
        final GlobalBiology globalBiology = new GlobalBiology(species1);

        final List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();

        final HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 5, 1}, {1, 2, 3}});
        localBiologies.add(new AbundanceLocalBiology(abundance));

        abundance.put(species1, new double[][]{{20, 30, 40}, {0, 0, 1}});
        localBiologies.add(new AbundanceLocalBiology(abundance));


        //  final FishState fishState = mock(FishState.class);


        assertEquals(
            30,
            aggregator.apply(globalBiology, localBiologies).getAbundance(species1).asMatrix()[0][0],
            0
        );
        assertEquals(
            1,
            aggregator.apply(globalBiology, localBiologies).getAbundance(species1).asMatrix()[1][0],
            0
        );
    }
}