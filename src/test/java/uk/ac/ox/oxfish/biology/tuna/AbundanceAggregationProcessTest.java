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

        HashMap<Species, double[][]> abundance = new HashMap<>();
        double[][] abundValues = {{4, 1, 1}, {1, 1, 1}, {1, 1, 1}};
//        double[][] abundValues = {{1}};
        abundance.put(species1, abundValues);

        List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();
        //Collections.EMPTY_LIST;
        localBiologies.add(new AbundanceLocalBiology(abundance));



      //  final FishState fishState = mock(FishState.class);



        assertEquals(
                9,
                aggregator.aggregate(globalBiology, localBiologies).getAbundance(species1).asMatrix()[0][0],
                0
        );
    }
}