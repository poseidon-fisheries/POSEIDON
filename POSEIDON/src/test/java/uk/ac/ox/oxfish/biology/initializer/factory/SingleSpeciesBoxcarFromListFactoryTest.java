package uk.ac.ox.oxfish.biology.initializer.factory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ec.util.MersenneTwisterFast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jfree.util.Log;
import org.jfree.util.LogTarget;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SingleSpeciesBoxcarFromListFactoryTest {




    @Test
    public void putsInThePopulationWeInputted() {


        //puts in the population we put out
        double[] populationArray = {1000.6980456204348, 6.121015839632853, 25.398188565908676, 66.43767691477537, 123.49416680518418, 175.51312136883783, 203.44393523581397, 207.9838081314201, 206.16942630531207, 212.77311507927917, 229.68682224793372, 251.6788443323218, 276.15140969414256, 305.3418840261694, 343.385307462176, 395.53623960448533, 471.56585072519, 594.8225897076568, 837.8882026922994, 1622.2151371353784, 2951.4561774929157, 0.0, 0.0, 0.0, 0.0};
        final SingleSpeciesAbundanceInitializer equalSpaced = buildPopulation(populationArray);
        System.out.println(Arrays.deepToString(equalSpaced.
                getInitialAbundance().getInitialAbundance()));


        assertArrayEquals(equalSpaced.
                getInitialAbundance().getInitialAbundance()[0],
                populationArray,
                .1
                );
    }

    @Test
    public void tooFewBins() {

        // Add a fake logger
        final LogTarget logTarget = mock(LogTarget.class);
        Log.getInstance().addTarget(logTarget);

        final double[] populationArray = new double[2];
        assertThrows(
            "bins do not reach even half of L_infinity. The biology is inconsistent!",
            IllegalArgumentException.class,
            () -> buildPopulation(populationArray)
        );

        final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(logTarget).log(anyInt(), captor.capture());
        assertTrue(captor.getAllValues().contains(
            "The number of bins provided given their width won't reach l-infinity..."
        ));

    }

    @NotNull
    private SingleSpeciesAbundanceInitializer buildPopulation(double[] populationArray) {
        final List<Double> population = new ArrayList<>();
        final FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());
        for (double pop : populationArray) {
            population.add(pop);
        }
        SingleSpeciesBoxcarFromListFactory control2 = new SingleSpeciesBoxcarFromListFactory();
        control2.setCmPerBin(5);
        control2.setLInfinity(new FixedDoubleParameter(100));
        control2.setVirginRecruits(new FixedDoubleParameter(1000));
        control2.setInitialNumbersInEachBin(population);
        final SingleSpeciesAbundanceInitializer equalSpaced = control2.apply(mock);
        final GlobalBiology globalBiology = equalSpaced.generateGlobal(new MersenneTwisterFast(), mock);
        equalSpaced.getInitialAbundance().initialize(globalBiology.getSpecie(0));
        return equalSpaced;
    }
}