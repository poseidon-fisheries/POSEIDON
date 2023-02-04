package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LocalizedActionCounterTest {


    @Test
    public void countsRight() {

        AbstractFadSetAction validAction = mock(AbstractFadSetAction.class);
        //optional are final classes which makes all this pain necessary.
        Catch validCatch = mock(Catch.class);
        when(validCatch.getTotalWeight()).thenReturn(10d);
        Optional<Catch> catchesKept = Optional.of(validCatch);
        when(validAction.getCatchesKept()).thenReturn(catchesKept);

        AbstractFadSetAction invalidAction = mock(AbstractFadSetAction.class);
        //optional are final classes which makes all this pain necessary.
        Catch invalidCatch = mock(Catch.class);
        when(invalidCatch.getTotalWeight()).thenReturn(20d);
        Optional<Catch> catchesKeptInvalid = Optional.of(invalidCatch);
        when(invalidAction.getCatchesKept()).thenReturn(catchesKeptInvalid);

        LocalizedActionCounter counter = new LocalizedActionCounter(
                abstractFadSetAction -> abstractFadSetAction.equals(validAction),
                "testcounter"
        );

        FishState fake = mock(FishState.class,RETURNS_DEEP_STUBS);
        counter.start(fake);
        verify(fake.getYearlyDataSet(),times(2)).registerGatherer(anyString(),any(),anyDouble());

        counter.observe(validAction);
        counter.observe(validAction);
        counter.observe(invalidAction);
        counter.observe(invalidAction);

        assertEquals(counter.getNumberOfActionsThisYearSoFar(),2,.001);
        assertEquals(counter.getTotalCatchThisYearSoFar(),20,.001);

    }
}