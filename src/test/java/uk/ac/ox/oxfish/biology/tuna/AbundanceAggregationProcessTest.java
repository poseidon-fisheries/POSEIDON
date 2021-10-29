package uk.ac.ox.oxfish.biology.tuna;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class AbundanceAggregationProcessTest {

    @Test
    public void AbundanceAggregationProcessTester(){
        //final BiologicalProcess<AbundanceLocalBiology> aggregationProcess = new AbundanceAggregationProcess();
        AbundanceAggregator aggregator = new AbundanceAggregator(false, true);
        Species species1 = new Species("Piano Tuna");
        GlobalBiology globalBiology = new GlobalBiology(species1);

        List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();

        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 5, 1}, {1, 2, 3}});
        localBiologies.add(new AbundanceLocalBiology(abundance));

        abundance.put(species1, new double[][]{{20, 30, 40}, {0, 0, 1}});
        localBiologies.add(new AbundanceLocalBiology(abundance));


      //  final FishState fishState = mock(FishState.class);



        assertEquals(
                30,
                aggregator.aggregate(globalBiology, localBiologies).getAbundance(species1).asMatrix()[0][0],
                0
        );
        assertEquals(
                1,
                aggregator.aggregate(globalBiology, localBiologies).getAbundance(species1).asMatrix()[1][0],
                0
        );
    }
}