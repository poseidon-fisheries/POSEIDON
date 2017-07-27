package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/27/17.
 */
public class QuotaLimitDecoratorTest {


    @Test
    public void decoratesCorrectly() throws Exception {

        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(),any(),any(),any())).thenReturn(false);

        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);

        //short-circuit to false if the decorated stub always returns false!
        assertFalse(decorator.shouldFish(mock(Fisher.class),new MersenneTwisterFast(),
                                         mock(FishState.class),mock(TripRecord.class)));

    }

    @Test
    public void anarchyNeverGetsStopped() throws Exception {

        //two species world
        Species species1 = new Species("one");
        Species species2 = new Species("two");
        GlobalBiology biology = new GlobalBiology(species1, species2);
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(biology.getSpecies());


        //you have quotas for 100kg a species
        Regulation regulation = new Anarchy();
        Fisher fisher = mock(Fisher.class);
        when(fisher.getRegulation()).thenReturn(regulation);

        //hold
        Hold hold = new Hold(500,biology);
        hold.load(new Catch(new double[]{300,200})); //very full load!
        when(fisher.getHold()).thenReturn(hold);


        //decorator! (decorated always returns true so it's not important)
        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(),any(),any(),any())).thenReturn(true);
        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),
                                        model,mock(TripRecord.class)));







    }

    @Test
    public void quotaRules() throws Exception {

        //two species world
        Species species1 = new Species("one");
        Species species2 = new Species("two");
        GlobalBiology biology = new GlobalBiology(species1, species2);
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(biology.getSpecies());



        //you have quotas for 100kg a species
        MultiQuotaRegulation regulation = new MultiQuotaRegulation(new double[]{100,100},model);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getRegulation()).thenReturn(regulation);

        //hold
        Hold hold = new Hold(500,biology);
        hold.load(new Catch(new double[]{0,0})); //empty!
        when(fisher.getHold()).thenReturn(hold);

        //decorator! (decorated always returns true so it's not important)
        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(),any(),any(),any())).thenReturn(true);

        //there is nothing in the hold so the decorator should return true
        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),
                                        model,mock(TripRecord.class)));

        //load up a bit more but still not enough
        hold.load(new Catch(new double[]{50,50})); //getting full
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),
                                        model,mock(TripRecord.class)));

        //even still, but having exactly the right amount doesn't stop you from fishing
        hold.load(new Catch(new double[]{50,0})); //getting full
        assertTrue(decorator.shouldFish(fisher,new MersenneTwisterFast(),
                                        model,mock(TripRecord.class)));

        //but overflowing in one category does make you want to go back!
        hold.load(new Catch(new double[]{50,0})); //getting full
        assertFalse(decorator.shouldFish(fisher,new MersenneTwisterFast(),
                                        model,mock(TripRecord.class)));
    }
}