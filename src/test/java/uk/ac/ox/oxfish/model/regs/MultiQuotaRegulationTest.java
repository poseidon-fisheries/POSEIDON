package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MultiQuotaRegulationTest {



    @Test
    public void multiQuotaRespectsMPAs() throws Exception {

        FishState model = mock(FishState.class);
        MultiQuotaRegulation regs = new MultiQuotaRegulation(new double[]{1d,2d}, model);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero"); zero.resetIndexTo(0);
        Species one = new Species("one"); one.resetIndexTo(1);


        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1,regs.getQuotaRemaining(0),.0001);
        assertEquals(2,regs.getQuotaRemaining(1),.0001);


        SeaTile tile = mock(SeaTile.class);
        when(tile.isProtected()).thenReturn(false);
        assertTrue(regs.canFishHere(fisher, tile, model));

        when(tile.isProtected()).thenReturn(true);
        assertFalse(regs.canFishHere(fisher, tile, model));

        //if it's not protected but you are out of quota, you can't fish
        when(tile.isProtected()).thenReturn(false);
        regs.setQuotaRemaining(0,0);
        assertFalse(regs.canFishHere(fisher, tile, model));


    }


    @Test
    public void multiQuotaGen() throws Exception {


        FishState model = mock(FishState.class);
        MultiQuotaRegulation regs = new MultiQuotaRegulation(new double[]{1d,2d}, model);
        Fisher fisher = mock(Fisher.class);
        Species zero = new Species("zero"); zero.resetIndexTo(0);
        Species one = new Species("one"); one.resetIndexTo(1);


        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1,regs.getQuotaRemaining(0),.0001);
        assertEquals(2,regs.getQuotaRemaining(1),.0001);

        assertEquals(1,regs.maximumBiomassSellable(fisher,zero, model),.0001);
        assertEquals(2,regs.maximumBiomassSellable(fisher, one, model),.0001);

        //sell one unit of specie 1
        regs.reactToSale(one,fisher,1,123141);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1,regs.getQuotaRemaining(0),.0001);
        assertEquals(1,regs.getQuotaRemaining(1),.0001);

        assertEquals(1,regs.maximumBiomassSellable(fisher,zero, model),.0001);
        assertEquals(1,regs.maximumBiomassSellable(fisher,one, model),.0001);


        //sell another, now you are not allowed to fish
        regs.reactToSale(one,fisher,1,123141);

        assertFalse(regs.allowedAtSea(fisher, model));
        assertEquals(1,regs.getQuotaRemaining(0),.0001);
        assertEquals(0,regs.getQuotaRemaining(1),.0001);

        assertEquals(1,regs.maximumBiomassSellable(fisher,zero, model),.0001);
        assertEquals(0,regs.maximumBiomassSellable(fisher,one, model),.0001);

        //reset after step
        regs.step(model);

        assertTrue(regs.allowedAtSea(fisher, model));
        assertEquals(1,regs.getQuotaRemaining(0),.0001);
        assertEquals(2,regs.getQuotaRemaining(1),.0001);

        assertEquals(1,regs.maximumBiomassSellable(fisher,zero, model),.0001);
        assertEquals(2,regs.maximumBiomassSellable(fisher, one, model),.0001);





    }
}