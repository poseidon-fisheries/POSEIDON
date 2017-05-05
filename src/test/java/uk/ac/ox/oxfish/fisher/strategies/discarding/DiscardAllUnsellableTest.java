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
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/3/17.
 */
public class DiscardAllUnsellableTest {

    private final FishState model = mock(FishState.class);
    private Fisher fisher= mock(Fisher.class);

    @Test
    public void noDiscarding() throws Exception {

        Species species1 = new Species("first", Meristics.FAKE_MERISTICS);
        Species species2 = new Species("second", Meristics.FAKE_MERISTICS);

        GlobalBiology biology = new GlobalBiology(species1, species2);
        when(model.getSpecies()).thenReturn(biology.getSpecies());

        Catch caught = new Catch(species1, 100, biology);
        DiscardAllUnsellable noDiscarding = new DiscardAllUnsellable();
        noDiscarding.setSafetyBuffer(0d);

        Regulation mock = mock(Regulation.class);
        //no discarding should occur
        when(mock.maximumBiomassSellable(fisher,species1,model)).thenReturn(200d);
        Catch retained = noDiscarding.chooseWhatToKeep(mock(SeaTile.class),
                                                       fisher,
                                                       caught,
                                                       1,
                                                       mock,
                                                       model,
                                                       new MersenneTwisterFast());

        //assertEquals(retained,caught); not true because the objects are different!
        assertEquals(retained.getTotalWeight(),caught.getTotalWeight(),.001);
        assertEquals(retained.getWeightCaught(0),caught.getWeightCaught(0),.001);
        assertEquals(retained.getWeightCaught(1),caught.getWeightCaught(1),.001);

    }

    @Test
    public void someDiscarding() throws Exception {

        Species species1 = new Species("first", Meristics.FAKE_MERISTICS);
        Species species2 = new Species("second", Meristics.FAKE_MERISTICS);

        GlobalBiology biology = new GlobalBiology(species1, species2);
        when(model.getSpecies()).thenReturn(biology.getSpecies());
        Catch caught = new Catch(species1, 100, biology);
        DiscardAllUnsellable noDiscarding = new DiscardAllUnsellable();
        noDiscarding.setSafetyBuffer(0d);

        Regulation mock = mock(Regulation.class);
        //no discarding should occur
        when(mock.maximumBiomassSellable(fisher,species1,model)).thenReturn(50d);
        Catch retained = noDiscarding.chooseWhatToKeep(mock(SeaTile.class),
                                                       fisher,
                                                       caught,
                                                       1,
                                                       mock,
                                                       model,
                                                       new MersenneTwisterFast());

        assertNotEquals(retained,caught);
        assertEquals(retained.getWeightCaught(0),50d,.001);
        assertEquals(retained.getWeightCaught(1),0d,.001);
        assertEquals(caught.getWeightCaught(0),100d,.001);

    }


}