package uk.ac.ox.oxfish.biology.tuna;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.TunaMeristics;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public class AbundanceMortalityProcessTest {

    @Test
    public void AbundanceMortalityProcessTester(){

        List<double[]> weights = new ArrayList<>();
        weights.add(new double[] {30,30});
        weights.add(new double[] {30,30});
        List<double[]> lengths = new ArrayList<>();
        lengths.add(new double[] {20,20});
        lengths.add(new double[] {20,20});
        List<double[]> proportionalMortalities = new ArrayList<>();
        proportionalMortalities.add(new double[] {.25,.35});
        proportionalMortalities.add(new double[] {.5,.75});

        TunaMeristics meristics = new TunaMeristics(weights, lengths, proportionalMortalities, new double[]{10,10});

        Species species1 = new Species("Piano Tuna", meristics);

        Map<String, IntFunction<SmallLargeAllocationGridsSupplier.SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("Piano Tuna", entry -> entry==0?SMALL:LARGE );

        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
                seaTile.setBiology(new AbundanceLocalBiology(abundance)
                )
        );

        final BiologicalProcess<AbundanceLocalBiology> mortalityProcess =
            new AbundanceMortalityProcess(
                ImmutableMap.of(
                    species1,
                    ImmutableMap.of(
                        "test",
                        proportionalMortalities
                            .stream()
                            .map(a -> stream(a).boxed().collect(toList()))
                            .collect(toList())
                    )
                )
            );

        List<SeaTile> allSeaTiles = nauticalMap.getAllSeaTilesAsList();

        Collection<AbundanceLocalBiology> localBiologies = new ArrayList<>();
        for (SeaTile allSeaTile : allSeaTiles) {
            localBiologies.add((AbundanceLocalBiology) allSeaTile.getBiology());
        }
        final List<AbundanceLocalBiology> biologiesAfterMortality =
            ImmutableList.copyOf(mortalityProcess.process(mock(FishState.class), localBiologies));

        assertEquals(
            7.5,
            biologiesAfterMortality.get(0).getAbundance(species1).asMatrix()[0][0],
            0
        );
        assertEquals(
            6.5,
            biologiesAfterMortality.get(1).getAbundance(species1).asMatrix()[0][1],
            0
        );
        assertEquals(
            5,
            biologiesAfterMortality.get(2).getAbundance(species1).asMatrix()[1][0],
            0
        );
        assertEquals(
            2.5,
            biologiesAfterMortality.get(3).getAbundance(species1).asMatrix()[1][1],
            0
        );
    }

}