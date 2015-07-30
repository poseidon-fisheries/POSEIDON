package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.geography.SeaTile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FixedProportionGearTest {


    @Test
    public void fishEmpty() throws Exception {
        Specie first = new Specie("First");
        Specie second = new Specie("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = new EmptyLocalBiology();
        SeaTile where = new SeaTile(0,0,-100);
        where.setBiology(local);

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 0, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 0, .001);

    }

    @Test
    public void fishOnlyWhatIsAvailable()
    {
        Specie first = new Specie("First");
        Specie second = new Specie("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(100.0);
        when(local.getBiomass(second)).thenReturn(0.0);

        SeaTile where = new SeaTile(0,0,-100);
        where.setBiology(local);

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 50, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 0, .001);
        verify(local).reactToThisAmountOfBiomassBeingFished(first, 50.0);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(second,0.0);

    }

    @Test
    public void fishOnlyWhatIsAvailable2()
    {
        Specie first = new Specie("First");
        Specie second = new Specie("Second");
        GlobalBiology biology = new GlobalBiology(first, second);
        LocalBiology local = mock(LocalBiology.class);
        when(local.getBiomass(first)).thenReturn(0.0);
        when(local.getBiomass(second)).thenReturn(100.0);

        SeaTile where = new SeaTile(0,0,-100);
        where.setBiology(local);

        FixedProportionGear gear = new FixedProportionGear(.5);
        Catch fishCaught = gear.fish(mock(Fisher.class), where,1 , biology);

        assertEquals(fishCaught.getPoundsCaught(first), 0, .001);
        assertEquals(fishCaught.getPoundsCaught(second), 50, .001);
        verify(local,never()).reactToThisAmountOfBiomassBeingFished(first, 0.0);
        verify(local).reactToThisAmountOfBiomassBeingFished(second, 50.0);

    }
}