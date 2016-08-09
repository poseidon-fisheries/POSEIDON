package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HeterogeneousAbundanceGearTest
{


    @Test
    public void catchesCorrectly() throws Exception {

        Species species1 = mock(Species.class);
        when(species1.getIndex()).thenReturn(0);
        Species species2 = mock(Species.class);
        when(species2.getIndex()).thenReturn(1);


        HomogeneousAbundanceGear gear1 = mock(HomogeneousAbundanceGear.class);
        when(gear1.fishThisSpecies(any(),any(),anyBoolean())).thenReturn(100d);
        HomogeneousAbundanceGear gear2 = mock(HomogeneousAbundanceGear.class);
        when(gear2.fishThisSpecies(any(),any(),anyBoolean())).thenReturn(200d);

        HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
                new Pair<>(species1,gear1),
                new Pair<>(species2,gear2)
        );

        GlobalBiology biology = new GlobalBiology(species1,species2);

        SeaTile mock = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(mock.getBiology().getBiomass(any())).thenReturn(1d);
        Catch caught = gear.fish(mock(Fisher.class), mock, 2, biology);
        assertEquals(caught.getPoundsCaught(0),200,.001);
        assertEquals(caught.getPoundsCaught(1),400,.001);


    }
}