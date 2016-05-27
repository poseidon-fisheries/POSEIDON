package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import org.junit.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class NumberOfFishersHereExtractorTest {


    @Test
    public void safeifEmpty() throws Exception {


        SeaTile full = mock(SeaTile.class);
        SeaTile empty = mock(SeaTile.class);
        SeaTile halfFull = mock(SeaTile.class);
        List<SeaTile> toRepresent = Arrays.asList(full, empty,halfFull);



        FishState model = mock(FishState.class);
        when(model.getFishersAtLocation(empty)).thenReturn(new Bag());
        Bag halfFullBag = new Bag();
        halfFullBag.add(new Object());
        halfFullBag.add(new Object());
        halfFullBag.add(new Object());
        when(model.getFishersAtLocation(halfFull)).thenReturn(halfFullBag);
        Bag fullBag = new Bag();
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        when(model.getFishersAtLocation(full)).thenReturn(fullBag);


        NumberOfFishersHereExtractor safe = new NumberOfFishersHereExtractor();
        HashMap<SeaTile, Double> safetyReport = safe.extractFeature(toRepresent, model,
                                                                    mock(Fisher.class)
        );

        assertTrue(safetyReport.get(empty)==0);
        assertTrue(safetyReport.get(halfFull)==3);
        assertTrue(safetyReport.get(full)==5);


    }
}