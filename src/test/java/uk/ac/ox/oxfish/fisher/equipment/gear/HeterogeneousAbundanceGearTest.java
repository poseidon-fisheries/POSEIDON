package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.Pair;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.MALE;


public class HeterogeneousAbundanceGearTest
{


    @Test
    public void catchesCorrectly() throws Exception {

        Species species1 = new Species("longspine1",new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                 0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                 0.111313, 17.826, -1.79, 1,
                                                                 0, 168434124,
                                                                 0.6, false));
        species1.resetIndexTo(0);
        Species species2 = new Species("longspine2",new Meristics(80, 40, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                  0.111313, 3, 8.573, 27.8282, 0.108505, 4.30E-06, 3.352,
                                                                  0.111313, 17.826, -1.79, 1,
                                                                  0, 168434124,
                                                                  0.6, false));
        species2.resetIndexTo(1);


        HomogeneousAbundanceGear gear1 = mock(HomogeneousAbundanceGear.class,RETURNS_DEEP_STUBS);
        int[][] catches = new int[2][81];
        catches[0][5]=1000; //total catch weight = 19.880139
        when(gear1.catchesAsAbundanceForThisSpecies(any(),anyInt(), any())).
                thenReturn(
                new StructuredAbundance(catches[MALE],catches[FEMALE])
        );
        HomogeneousAbundanceGear gear2 = mock(HomogeneousAbundanceGear.class,RETURNS_DEEP_STUBS);
        int[][] catches2 = new int[2][81];
        catches2[0][5]=2000; //total catch weight = 19.880139*2
        when(gear2.catchesAsAbundanceForThisSpecies(any(),anyInt(), any())).thenReturn(
                new StructuredAbundance(catches2[MALE],catches2[FEMALE])
        );


        HeterogeneousAbundanceGear gear = new HeterogeneousAbundanceGear(
                new Pair<>(species1,gear1),
                new Pair<>(species2,gear2)
        );

        GlobalBiology biology = new GlobalBiology(species1,species2);

        SeaTile mock = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(mock.getBiology().getBiomass(any())).thenReturn(1d);
        Catch caught = gear.fish(mock(Fisher.class), mock, 1, biology);
        assertEquals(caught.getWeightCaught(0), 19.880139, .001);
        assertEquals(caught.getWeightCaught(1), 19.880139*2, .001);


    }
}