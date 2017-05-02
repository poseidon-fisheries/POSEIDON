package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class FixedProportionGearTest {


    @Test
    public void fishEmpty() throws Exception {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = new EmptyLocalBiology();
        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getWeightCaught(first), 0, .001);
        assertEquals(fishCaught.getWeightCaught(second), 0, .001);

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

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getWeightCaught(first), 50, .001);
        assertEquals(fishCaught.getWeightCaught(second), 0, .001);
        //gear itself never calls biology reacts
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());

    }

    @Test
    public void expectedCatchesDoNotKillFish()
    {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0,0,-100, new TileHabitat(0d));
        where.setBiology(local);

        FixedProportionGear gear = new FixedProportionGear(.5);
        double[] fishCaught = gear.expectedHourlyCatch(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught[0], 50, .001);
        assertEquals(fishCaught[1], 0, .001);
        //gear itself never calls biology reacts
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());

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

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getWeightCaught(first), 0, .001);
        assertEquals(fishCaught.getWeightCaught(second), 50, .001);
        //gear itself never calls biology reacts
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(any(),any(),any());

    }
}