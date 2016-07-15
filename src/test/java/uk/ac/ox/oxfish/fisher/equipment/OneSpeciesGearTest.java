package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class OneSpeciesGearTest {



    @Test
    public void fishEmpty() throws Exception {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = new EmptyLocalBiology();
        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first,.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 0, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 0, .001);

    }

    @Test
    public void fishOnlyWhatIsAvailable()
    {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first,.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 50, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 0, .001);
        verify(local).reactToThisAmountOfBiomassBeingFished(first, 50.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second,0.0);

    }


    @Test
    public void expectationKillsNoFish()
    {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first,.5);
        double[] fishCaught = gear.expectedHourlyCatch(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught[0], 50, .001);
        assertEquals(fishCaught[1], 0, .001);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(first, 50.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second,0.0);

    }

    @Test
    public void fishOnlyWhatIsAvailable2()
    {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(0.0);
        when(local.getBiomass(second)).thenReturn(100.0);

        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        OneSpecieGear gear = new OneSpecieGear(first,.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 0, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 0, .001);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(first, 0.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second, 0.0);

    }
    
    
    

}