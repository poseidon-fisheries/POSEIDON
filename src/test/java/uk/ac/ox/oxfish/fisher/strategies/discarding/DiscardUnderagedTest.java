package uk.ac.ox.oxfish.fisher.strategies.discarding;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/12/17.
 */
public class DiscardUnderagedTest {


    @Test
    public void discardUnderaged() throws Exception {


        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first", first);
        Species secondSpecies = new Species("second",second);


        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);

        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d,100d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );


        Catch haul = new Catch(
                new int[]{100,10,1}, new int[]{100,0,0},
                firstSpecies,
                bio
        );
        DiscardUnderaged diskards= new DiscardUnderaged(1);
        FishState model = mock(FishState.class); when(model.getBiology()).thenReturn(bio);
        Catch newCatch = diskards.chooseWhatToKeep(
                mock(SeaTile.class),
                mock(Fisher.class),
                haul,
                1000,
                mock(Regulation.class),
                model,
                new MersenneTwisterFast()
        );

        assertArrayEquals(new int[]{0,10,1},
                          newCatch.getAbundance(firstSpecies).getAbundance()[FishStateUtilities.MALE]);
        assertArrayEquals(new int[]{0,0,0},
                          newCatch.getAbundance(firstSpecies).getAbundance()[FishStateUtilities.FEMALE]);
        assertArrayEquals(new int[]{0,0},
                          newCatch.getAbundance(secondSpecies).getAbundance()[FishStateUtilities.FEMALE]);

    }
}