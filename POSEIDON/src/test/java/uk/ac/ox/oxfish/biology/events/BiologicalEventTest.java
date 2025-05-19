/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.events;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 10/7/16.
 */
public class BiologicalEventTest {


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void calledTheRightAmountOfTimes() throws Exception {

        //the event increases this number
        final AtomicInteger counter = new AtomicInteger(0);

        final FishState state = MovingTest.generateSimple4x4Map();

        final Predicate trigger = mock(Predicate.class);
        when(trigger.test(any())).thenReturn(true, false, true);

        final Predicate<SeaTile> mock = mock(Predicate.class);
        doAnswer(invocation -> ((SeaTile) invocation.getArguments()[0]).getGridX() == 0).when(mock).test(any());

        final BiologicalEvent event = new BiologicalEvent(

            trigger,
            mock,
            tile -> counter.incrementAndGet()

        );

        event.step(state);
        event.step(state);
        event.step(state);

        //only one row trigger, and it only triggers twice
        Assertions.assertEquals(counter.get(), 8);


    }
}
