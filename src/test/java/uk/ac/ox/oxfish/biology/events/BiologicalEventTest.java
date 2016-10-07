package uk.ac.ox.oxfish.biology.events;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 10/7/16.
 */
public class BiologicalEventTest {


    @Test
    public void calledTheRightAmountOfTimes() throws Exception {

        //the event increases this number
        AtomicInteger counter = new AtomicInteger(0);

        FishState state = MovingTest.generateSimple4x4Map();

        Predicate trigger = mock(Predicate.class);
        when(trigger.test(any())).thenReturn(true,false,true);

        Predicate<SeaTile> mock = mock(Predicate.class);
        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return ((SeaTile) invocation.getArguments()[0]).getGridX()==0;
            }
        }).when(mock).test(any());

        BiologicalEvent event = new BiologicalEvent(

                trigger,
                mock,
                tile -> counter.incrementAndGet()

        );

        event.step(state);
        event.step(state);
        event.step(state);

        //only one row trigger, and it only triggers twice
        assertEquals(counter.get(),8);


    }
}