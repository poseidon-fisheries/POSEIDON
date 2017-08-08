package uk.ac.ox.oxfish.fisher.strategies.discarding;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/23/17.
 */
public class AlwaysDiscardTheseSpeciesTest {


    @Test
    public void discard() throws Exception {

        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1,2);
        Catch original = new Catch(new double[]{100,100,100});
        Catch postDiscard = strategy.chooseWhatToKeep(
                null,
                null,
                original,
                0,
                null,
                null,
                null
        );

        assertEquals(original.getTotalWeight(),300.0,.0001);
        assertEquals(postDiscard.getTotalWeight(),100.0,.0001);

        assertEquals(original.getWeightCaught(0),100.0,.0001);
        assertEquals(postDiscard.getWeightCaught(0),100.0,.0001);

        assertEquals(original.getWeightCaught(1),100.0,.0001);
        assertEquals(postDiscard.getWeightCaught(1),0,.0001);


    }


    @Test
    public void discardAbundance() throws Exception {

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

        AlwaysDiscardTheseSpecies strategy = new AlwaysDiscardTheseSpecies(1);
        int maleFirst[] = new int[]{100,100,100};
        int femaleFirst[] = new int[]{20,20,20};
        int maleSecond[] = new int[]{20,20};
        int femaleSecond[] = new int[]{20,20};
        Catch original = new Catch(
                new int[][]{maleFirst,maleSecond},
                new int[][]{femaleFirst,femaleSecond},
                bio
        );
        FishState model = mock(FishState.class);
        when(model.getBiology()).thenReturn(bio);
        Catch end = strategy.chooseWhatToKeep(null, null, original, 1,
                                                 mock(Regulation.class),
                                                 model,
                                                 new MersenneTwisterFast());
        Assert.assertEquals(end.getWeightCaught(secondSpecies),0,.0001);
        assertTrue(end.getWeightCaught(firstSpecies)>0);
        assertEquals(end.getAbundance(firstSpecies).getAbundance()
                             [FishStateUtilities.MALE][1],100);
        assertEquals(end.getAbundance(firstSpecies).getAbundance()
                             [FishStateUtilities.FEMALE][1],20);


    }
}