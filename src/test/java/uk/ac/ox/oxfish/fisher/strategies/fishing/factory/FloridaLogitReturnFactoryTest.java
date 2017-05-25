package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.DailyReturnDecorator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.LogitReturnStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.MaximumDaysDecorator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/25/17.
 */
public class FloridaLogitReturnFactoryTest {


    @Test
    public void logitReturnsRightProbability() throws Exception {
        //scaled $/kg
        FloridaLogitReturnFactory handlinerFishingStrategy = new FloridaLogitReturnFactory();
        handlinerFishingStrategy.setIntercept(new FixedDoubleParameter(-3.47701));
        handlinerFishingStrategy.setPriceRedGrouper(new FixedDoubleParameter(0.92395 ));
        handlinerFishingStrategy.setPriceGagGrouper(new FixedDoubleParameter(-0.65122));
        handlinerFishingStrategy.setRatioCatchToFishHold(new FixedDoubleParameter(4.37828));
        handlinerFishingStrategy.setWeekendDummy(new FixedDoubleParameter(-0.24437));

        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        Species gag = mock(Species.class);
        when((state.getBiology().getSpecie("GagGrouper"))).thenReturn(gag);
        Species red = mock(Species.class);
        when((state.getBiology().getSpecie("RedGrouper"))).thenReturn(red);
        DailyReturnDecorator strategy = handlinerFishingStrategy.apply(state);
        LogitReturnStrategy logit = (LogitReturnStrategy) ((MaximumDaysDecorator) strategy.accessDecorated()).accessDecorated();



        SeaTile tile = mock(SeaTile.class);
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        when(fisher.getHomePort().getMarginalPrice(gag,fisher)).thenReturn(10d);
        when(fisher.getHomePort().getMarginalPrice(red,fisher)).thenReturn(3d);
        when(fisher.getTotalWeightOfCatchInHold()).thenReturn(100d);
        when(fisher.getMaximumHold()).thenReturn(200d);


        double probability = logit.getShouldIReturnClassifier().getProbability(
                fisher,
                30 * 24, //not a weekend!
                state,
                tile
        );
        System.out.println(probability);
        //grabbed from wolfram alpha
        assertEquals(0.006507830733273149,probability,.001);


    }
}