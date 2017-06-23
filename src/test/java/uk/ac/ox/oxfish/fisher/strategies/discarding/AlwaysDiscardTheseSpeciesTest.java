package uk.ac.ox.oxfish.fisher.strategies.discarding;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import static org.junit.Assert.*;

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
}