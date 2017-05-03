package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 5/3/17.
 */
public class NoDiscardingTest {


    @Test
    public void testing1() throws Exception {

        Species species1 = new Species("first", Meristics.FAKE_MERISTICS);
        Species species2 = new Species("second", Meristics.FAKE_MERISTICS);

        GlobalBiology biology = new GlobalBiology(species1,species2);

        Catch caught = new Catch(species1,100,biology);
        NoDiscarding noDiscarding = new NoDiscarding();

        Catch retained = noDiscarding.chooseWhatToKeep(mock(SeaTile.class),
                                                       mock(Fisher.class),
                                                       caught,
                                                       1,
                                                       mock(Regulation.class),
                                                       mock(FishState.class),
                                                       new MersenneTwisterFast());

        assertEquals(retained,caught);
        assertEquals(retained.getTotalWeight(),caught.getTotalWeight(),.001);
        assertEquals(retained.getWeightCaught(0),caught.getWeightCaught(0),.001);
        assertEquals(retained.getWeightCaught(1),caught.getWeightCaught(1),.001);

    }



}