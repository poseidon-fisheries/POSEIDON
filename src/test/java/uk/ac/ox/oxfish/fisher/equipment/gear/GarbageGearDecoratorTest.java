package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.utility.Pair;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by carrknight on 3/22/17.
 */
public class GarbageGearDecoratorTest {



    @Test
    public void fishEmpty() throws Exception {
        Species first = new Species("First");
        Species second = new Species("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = new EmptyLocalBiology();
        SeaTile where = new SeaTile(0, 0, -100, new TileHabitat(0d));
        where.setBiology(local);

        Gear gear = new GarbageGearDecorator(second,1.5,new OneSpecieGear(first,.5));
        Catch fishCaught = gear.fish(mock(Fisher.class), where, 1 , biology);

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

        Gear gear = new GarbageGearDecorator(second,2,new OneSpecieGear(first,.5));
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 50, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 100, .001);
        verify(local).reactToThisAmountOfBiomassBeingFished(first, 50.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second,100.0);

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

        Gear gear = new GarbageGearDecorator(second,2,new OneSpecieGear(first,.5));
        double[] fishCaught = gear.expectedHourlyCatch(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught[0], 50, .001);
        assertEquals(fishCaught[1], 100.0, .001);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(first, 50.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second,100.0);

    }


    @Test
    public void catchesCorrectly() throws Exception {

        Species species1 = mock(Species.class);
        when(species1.getIndex()).thenReturn(0);
        Species species2 = mock(Species.class);
        when(species2.getIndex()).thenReturn(1);
        Species species3 = mock(Species.class);
        when(species3.getIndex()).thenReturn(2);
        when(species3.isImaginary()).thenReturn(true);

        HomogeneousAbundanceGear gear1 = mock(HomogeneousAbundanceGear.class);
        when(gear1.fishThisSpecies(any(),any(),anyBoolean())).thenReturn(100d);
        HomogeneousAbundanceGear gear2 = mock(HomogeneousAbundanceGear.class);
        when(gear2.fishThisSpecies(any(),any(),anyBoolean())).thenReturn(200d);

        Gear gear = new GarbageGearDecorator(species3,.5,
                                                                   new HeterogeneousAbundanceGear(
                new Pair<>(species1, gear1),
                new Pair<>(species2,gear2)
        ));

        GlobalBiology biology = new GlobalBiology(species1,species2,species3);

        SeaTile mock = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(mock.getBiology().getBiomass(any())).thenReturn(1d);
        Catch caught = gear.fish(mock(Fisher.class), mock, 2, biology);
        assertEquals(caught.getPoundsCaught(0),200,.001);
        assertEquals(caught.getPoundsCaught(1),400,.001);
        assertEquals(caught.getPoundsCaught(2),300,.001);


    }
}