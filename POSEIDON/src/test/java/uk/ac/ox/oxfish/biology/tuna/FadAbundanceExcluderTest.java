package uk.ac.ox.oxfish.biology.tuna;

import bsh.util.NameCompletionTable;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.NauticalMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class FadAbundanceExcluderTest {

    @Test
    public void FadAbundanceExcluderTester(){

        Species species1 = new Species("Piano Tuna");

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        Map<String, IntFunction<SmallLargeAllocationGridsSupplier.SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("Piano Tuna", entry -> entry==0?SMALL:LARGE );

        final GlobalBiology globalBiology= new GlobalBiology(species1);
        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        HashMap<Species, double[][]> fadAbundance = new HashMap<>();
        fadAbundance.put(species1, new double[][]{{2, 1}, {3, 4}});


        AbundanceAggregator aggregator = new AbundanceAggregator();

        List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();
        localBiologies.add(new AbundanceLocalBiology(abundance));

        List<AbundanceLocalBiology> fadBiologies;
        fadBiologies = new ArrayList<>();
        fadBiologies.add(new AbundanceLocalBiology(fadAbundance));


        FadAbundanceExcluder fadAbundanceExcluder = new FadAbundanceExcluder();

        AbundanceLocalBiology excludedAbundance = fadAbundanceExcluder.exclude(new AbundanceLocalBiology(abundance), new AbundanceLocalBiology(fadAbundance));

        assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[0][0],8,0);
        assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[1][0],7,0);
        assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[0][1],9,0);
        assertEquals(excludedAbundance.getAbundance(species1).asMatrix()[1][1],6,0);

//        System.out.println("breakpoint");

    }


}