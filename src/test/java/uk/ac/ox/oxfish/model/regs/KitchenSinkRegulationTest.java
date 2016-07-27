package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class KitchenSinkRegulationTest {


    @Test
    public void simpleChecks() throws Exception {

        TemporaryProtectedArea mpa = mock(TemporaryProtectedArea.class);
        FishingSeason season = mock(FishingSeason.class);
        QuotaPerSpecieRegulation quota = mock(QuotaPerSpecieRegulation.class);

        KitchenSinkRegulation regs = new KitchenSinkRegulation(mpa,
                                                               season,
                                                               quota);


        //all true, return true
        when(mpa.canFishHere(any(),any(),any())).thenReturn(true);
        when(season.canFishHere(any(),any(),any())).thenReturn(true);
        when(quota.canFishHere(any(),any(),any())).thenReturn(true);
        assertTrue(regs.canFishHere(mock(Fisher.class),mock(SeaTile.class),mock(FishState.class)));
        //one false, return false
        when(mpa.canFishHere(any(),any(),any())).thenReturn(false);
        assertFalse(regs.canFishHere(mock(Fisher.class),mock(SeaTile.class),mock(FishState.class)));
        //two/three still false
        when(season.canFishHere(any(),any(),any())).thenReturn(true);
        assertFalse(regs.canFishHere(mock(Fisher.class),mock(SeaTile.class),mock(FishState.class)));
        when(quota.canFishHere(any(),any(),any())).thenReturn(true);
        assertFalse(regs.canFishHere(mock(Fisher.class),mock(SeaTile.class),mock(FishState.class)));



        //same exact process for "can I be out?"
        when(mpa.allowedAtSea(any(),any())).thenReturn(true);
        when(season.allowedAtSea(any(),any())).thenReturn(true);
        when(quota.allowedAtSea(any(),any())).thenReturn(true);
        assertTrue(regs.allowedAtSea(mock(Fisher.class),mock(FishState.class)));
        when(quota.allowedAtSea(any(),any())).thenReturn(false);
        assertFalse(regs.allowedAtSea(mock(Fisher.class),mock(FishState.class)));

        when(season.allowedAtSea(any(),any())).thenReturn(false);
        assertFalse(regs.allowedAtSea(mock(Fisher.class),mock(FishState.class)));

        when(mpa.allowedAtSea(any(),any())).thenReturn(false);
        assertFalse(regs.allowedAtSea(mock(Fisher.class),mock(FishState.class)));



        //check that calls get propagated
        regs.setQuotaRemaining(0,1d);
        verify(quota).setQuotaRemaining(0,1d);

        when(quota.getQuotaRemaining(123)).thenReturn(123d);
        assertEquals(123,regs.getQuotaRemaining(123),.0001);


        //take the minimum of the two
        when(season.maximumBiomassSellable(any(),any(),any())).thenReturn(100d);
        when(quota.maximumBiomassSellable(any(),any(),any())).thenReturn(200d);
        assertEquals(100,regs.maximumBiomassSellable(any(),any(),any()),.0001);
        when(quota.maximumBiomassSellable(any(),any(),any())).thenReturn(20d);
        assertEquals(20,regs.maximumBiomassSellable(any(),any(),any()),.0001);


    }



}