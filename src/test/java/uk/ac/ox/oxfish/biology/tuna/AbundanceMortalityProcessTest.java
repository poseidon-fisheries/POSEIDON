package uk.ac.ox.oxfish.biology.tuna;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class AbundanceMortalityProcessTest {

    @Test
    public void AbundanceMortalityProcessTester(){
        Species species1 = new Species("Piano Tuna");
        GlobalBiology globalBiology = new GlobalBiology(species1);

        List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();

        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 5, 1}, {1, 2, 3}});
        localBiologies.add(new AbundanceLocalBiology(abundance));

        abundance.put(species1, new double[][]{{20, 30, 40}, {0, 0, 1}});
        localBiologies.add(new AbundanceLocalBiology(abundance));


    }

}