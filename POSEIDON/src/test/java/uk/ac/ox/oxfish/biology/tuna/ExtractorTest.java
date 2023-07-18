package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.LARGE;
import static uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup.SMALL;
import static uk.ac.ox.oxfish.geography.TestUtilities.makeMap;

public class ExtractorTest {
    @Test
    public void ExtractionTester() {

        Species species1 = new Species("Piano Tuna");

        Map<String, String> sCodes = new HashMap<>();
        sCodes.put("SP1", species1.getName());
        SpeciesCodes speciesCodes = new SpeciesCodes(sCodes);

        Map<String, IntFunction<SmallLargeAllocationGridsSupplier.SizeGroup>> binToSizeGroupMappings = new HashMap<>();
        binToSizeGroupMappings.put("Piano Tuna", entry -> entry == 0 ? SMALL : LARGE);

        final GlobalBiology globalBiology = new GlobalBiology(species1);
        HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 10}, {10, 10}});

        final NauticalMap nauticalMap = makeMap(3, 3);
        nauticalMap.getAllSeaTilesAsList().forEach(seaTile ->
            seaTile.setBiology(new AbundanceLocalBiology(abundance)
            )
        );


        List<SeaTile> allSeaTiles = nauticalMap.getAllSeaTilesAsList();
        FishState fishState = mock(FishState.class);
        when(fishState.getSpecies()).thenReturn(globalBiology.getSpecies());
        when(fishState.getDayOfTheYear()).thenReturn(90);
        when(fishState.getMap()).thenReturn(nauticalMap);

        Extractor<LocalBiology> extractor = new Extractor<>(LocalBiology.class, false, true);

        List<LocalBiology> localBiologyList = extractor.apply(fishState);

        Assertions.assertEquals(localBiologyList.size(), 9);
        Assertions.assertEquals(localBiologyList.get(0).getAbundance(species1).asMatrix()[0][0], 10, .01);
    }
}