package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NullParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/25/17.
 */
public class FloridaLogisticDepartingFactoryTest {


    @Test
    public void nullables() throws Exception {
        FloridaLogisticDepartingFactory longlinerDepartingStrategy = new FloridaLogisticDepartingFactory();
        longlinerDepartingStrategy.setIntercept(new FixedDoubleParameter(-2.959116));
        longlinerDepartingStrategy.setSpring(new FixedDoubleParameter(0.770212));
        longlinerDepartingStrategy.setSummer(new FixedDoubleParameter(0.933939));
        longlinerDepartingStrategy.setWinter(new FixedDoubleParameter(0.706415));
        longlinerDepartingStrategy.setWeekend(new NullParameter());
        longlinerDepartingStrategy.setWindSpeedInKnots(new FixedDoubleParameter(0.004265));
        longlinerDepartingStrategy.setRealDieselPrice(new FixedDoubleParameter(-0.125913/ 0.219969157));
        longlinerDepartingStrategy.setPriceRedGrouper(new NullParameter());
        longlinerDepartingStrategy.setPriceGagGrouper(new NullParameter());

        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        DailyLogisticDepartingStrategy strategy = longlinerDepartingStrategy.apply(state);

        assertEquals(strategy.getClassifier().getSize(),6);


        longlinerDepartingStrategy.setIntercept(new FixedDoubleParameter(-2.075184));
        longlinerDepartingStrategy.setSpring(new FixedDoubleParameter(0.725026));
        longlinerDepartingStrategy.setSummer(new FixedDoubleParameter(0.624472));
        longlinerDepartingStrategy.setWinter(new FixedDoubleParameter(0.266862));
        longlinerDepartingStrategy.setWeekend(new FixedDoubleParameter(-0.097619));
        longlinerDepartingStrategy.setWindSpeedInKnots(new FixedDoubleParameter(-0.046672));
        longlinerDepartingStrategy.setRealDieselPrice(new FixedDoubleParameter(-0.515073 / 0.219969157));
        longlinerDepartingStrategy.setPriceRedGrouper(new FixedDoubleParameter(-0.3604 / 2.20462262));
        longlinerDepartingStrategy.setPriceGagGrouper(new FixedDoubleParameter(0.649616 / 2.20462262));


        strategy = longlinerDepartingStrategy.apply(state);
        assertEquals(strategy.getClassifier().getSize(),9);
    }


    @Test
    public void numbersAreCorrect() throws Exception {

        FloridaLogisticDepartingFactory longlinerDepartingStrategy = new FloridaLogisticDepartingFactory();
        longlinerDepartingStrategy.setIntercept(new FixedDoubleParameter(-2.075184));
        longlinerDepartingStrategy.setSpring(new FixedDoubleParameter(0.725026));
        longlinerDepartingStrategy.setSummer(new FixedDoubleParameter(0.624472));
        longlinerDepartingStrategy.setWinter(new FixedDoubleParameter(0.266862));
        longlinerDepartingStrategy.setWeekend(new FixedDoubleParameter(-0.097619));
        longlinerDepartingStrategy.setWindSpeedInKnots(new FixedDoubleParameter(-0.046672));
        longlinerDepartingStrategy.setRealDieselPrice(new FixedDoubleParameter(-0.515073 / 0.219969157));
        longlinerDepartingStrategy.setPriceRedGrouper(new FixedDoubleParameter(-0.3604 / 2.20462262));
        longlinerDepartingStrategy.setPriceGagGrouper(new FixedDoubleParameter(0.649616 / 2.20462262));


        FishState state = mock(FishState.class,RETURNS_DEEP_STUBS);
        Species gag = mock(Species.class);
        when((state.getBiology().getSpecie("GagGrouper"))).thenReturn(gag);
        DailyLogisticDepartingStrategy strategy = longlinerDepartingStrategy.apply(state);

        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(fisher.getHomePort().getGasPricePerLiter()).thenReturn(2d);
        double probability = strategy.getClassifier().getProbability(fisher,
                                                                     24 * 30, //winter!
                                                                     state,
                                                                     mock(SeaTile.class));
        System.out.println("probability: " + probability );

        assertEquals(0.0015140371942369245d,probability,.001);
        when(fisher.getHomePort().getMarginalPrice(gag,fisher)).thenReturn(10d);
        probability = strategy.getClassifier().getProbability(fisher,
                                                                     24 * 30, //winter!
                                                                     state,
                                                                     mock(SeaTile.class));
        System.out.println("probability: " + probability );
        assertEquals(0.0280627,probability,.001);
    }
}