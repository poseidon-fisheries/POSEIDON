package uk.ac.ox.oxfish.model.regs;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FishingSeasonTest {


    @Test
    public void fishHere() throws Exception {

        FishingSeason season = new FishingSeason(true,100);
        Fisher fisher = mock(Fisher.class);
        SeaTile tile = mock(SeaTile.class);
        FishState model = mock(FishState.class);

        //right season, not protected===> can fish
        when(model.getDayOfTheYear()).thenReturn(50);
        when(tile.isProtected()).thenReturn(false);
        assertTrue(season.canFishHere(fisher, tile, model));

        //off season ===> can't fish
        when(model.getDayOfTheYear()).thenReturn(150);
        when(tile.isProtected()).thenReturn(false);
        assertFalse(season.canFishHere(fisher, tile, model));

        //on season, protected ===> can't fish
        when(model.getDayOfTheYear()).thenReturn(50);
        when(tile.isProtected()).thenReturn(true);
        assertFalse(season.canFishHere(fisher, tile, model));
        //stop caring about mpas
        season = new FishingSeason(false,100);
        assertTrue(season.canFishHere(fisher, tile, model));

    }
}