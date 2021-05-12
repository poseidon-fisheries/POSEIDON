package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SingleSpeciesIrregularBoxcarFactoryTest {


    @Test
    public void irregularBoxcar() {
        final FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());



        //check that the length and weights match the regular ones
        SingleSpeciesRegularBoxcarFactory control = new SingleSpeciesRegularBoxcarFactory();
        control.setCmPerBin(5);
        final SingleSpeciesAbundanceInitializer equalSpaced = control.apply(mock);

        SingleSpeciesIrregularBoxcarFactory factory = new SingleSpeciesIrregularBoxcarFactory();
        factory.setBinnedLengthsInCm(Lists.newArrayList(12.5d,57.5d,102.5d));
        final SingleSpeciesAbundanceInitializer unequalSpaced = factory.apply(mock);

        //same lengths/same weights
        //12.5 cm
        assertEquals(
                equalSpaced.getMeristics().getLength(0,2),
                unequalSpaced.getMeristics().getLength(0,0),
                .0001);
        assertEquals(
                equalSpaced.getMeristics().getWeight(0,2),
                unequalSpaced.getMeristics().getWeight(0,0),
                .0001);
        //57.5
        assertEquals(
                equalSpaced.getMeristics().getLength(0,11),
                unequalSpaced.getMeristics().getLength(0,1),
                .0001);
        assertEquals(
                equalSpaced.getMeristics().getWeight(0,11),
                unequalSpaced.getMeristics().getWeight(0,1),
                .0001);

        //102.5
        assertEquals(
                equalSpaced.getMeristics().getLength(0,20),
                unequalSpaced.getMeristics().getLength(0,2),
                .0001);
        assertEquals(
                equalSpaced.getMeristics().getWeight(0,20),
                unequalSpaced.getMeristics().getWeight(0,2),
                .0001);


    }
}