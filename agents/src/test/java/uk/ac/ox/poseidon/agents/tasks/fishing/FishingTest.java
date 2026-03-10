/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.Task;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.ac.ox.poseidon.agents.catches.disposition.Disposition;
import uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.trips.Trip;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.events.EventManager;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FishingTest {

    private final Species a = new Species("A", null, "A");

    @Test
    void completePassesCatchThroughDispositionAndBroadcastsOutcome() throws Exception {
        final Vessel vessel = mock(Vessel.class);
        final Gear gear = mock(Gear.class);
        final Hold hold = mock(Hold.class);
        final DispositionProcess dispositionProcess = mock(DispositionProcess.class);
        final Fisheable fisheable = mock(Fisheable.class);
        final Trip trip = mock(Trip.class);
        final EventManager eventManager = mock(EventManager.class);

        final Bucket grossCatch = Bucket.of(a, Biomass.ofKg(10.0));
        final Bucket retained = Bucket.of(a, Biomass.ofKg(7.0));
        final Bucket discardedAlive = Bucket.of(a, Biomass.ofKg(3.0));
        final Disposition disposition = new Disposition(retained, discardedAlive, Bucket.empty());

        when(vessel.getGear()).thenReturn(gear);
        when(vessel.getHold()).thenReturn(hold);
        when(gear.fish(fisheable)).thenReturn(grossCatch);
        when(hold.getAvailableCapacityInKg()).thenReturn(42.0);
        when(dispositionProcess.partition(grossCatch, 42.0)).thenReturn(disposition);
        when(trip.getEventManager()).thenReturn(eventManager);

        final ExposedFishing fishing = new ExposedFishing(() -> fisheable, dispositionProcess);
        setTaskObject(fishing, vessel);
        setField(fishing, "trip", trip, fishing.getClass().getSuperclass().getSuperclass());
        setField(
            fishing,
            "action",
            mock(ExtendedFishingAction.class),
            fishing.getClass().getSuperclass()
        );

        final Task.Status status = fishing.invokeComplete();

        assertThat(status).isEqualTo(Task.Status.SUCCEEDED);
        verify(dispositionProcess).partition(grossCatch, 42.0);
        verify(hold).addContent(retained);
        verify(fisheable).release(discardedAlive);

        final ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventManager).broadcast(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(FishingEvent.class);
        final FishingEvent event = (FishingEvent) eventCaptor.getValue();
        assertThat(event.getOutcome().getGrossCatch()).isEqualTo(grossCatch);
        assertThat(event.getOutcome().getDisposition()).isEqualTo(disposition);
    }

    @Test
    void completeWorksForEmptyCatchAndEmptyDisposition() throws Exception {
        final Vessel vessel = mock(Vessel.class);
        final Gear gear = mock(Gear.class);
        final Hold hold = mock(Hold.class);
        final DispositionProcess dispositionProcess = mock(DispositionProcess.class);
        final Fisheable fisheable = mock(Fisheable.class);
        final Trip trip = mock(Trip.class);
        final EventManager eventManager = mock(EventManager.class);

        final Bucket grossCatch = Bucket.empty();
        final Disposition disposition = Disposition.empty();

        when(vessel.getGear()).thenReturn(gear);
        when(vessel.getHold()).thenReturn(hold);
        when(gear.fish(fisheable)).thenReturn(grossCatch);
        when(hold.getAvailableCapacityInKg()).thenReturn(0.0);
        when(dispositionProcess.partition(grossCatch, 0.0)).thenReturn(disposition);
        when(trip.getEventManager()).thenReturn(eventManager);

        final ExposedFishing fishing = new ExposedFishing(() -> fisheable, dispositionProcess);
        setTaskObject(fishing, vessel);
        setField(fishing, "trip", trip, fishing.getClass().getSuperclass().getSuperclass());
        setField(
            fishing,
            "action",
            mock(ExtendedFishingAction.class),
            fishing.getClass().getSuperclass()
        );

        final Task.Status status = fishing.invokeComplete();

        assertThat(status).isEqualTo(Task.Status.SUCCEEDED);
        verify(dispositionProcess).partition(grossCatch, 0.0);
        verify(hold).addContent(Bucket.empty());
        verify(fisheable).release(Bucket.empty());
        verify(eventManager).broadcast(any(FishingEvent.class));
    }

    private static final class ExposedFishing extends Fishing {
        private ExposedFishing(
            final Supplier<Fisheable> fisheableSupplier,
            final DispositionProcess dispositionProcess
        ) {
            super(fisheableSupplier, dispositionProcess);
        }

        private Task.Status invokeComplete() {
            return complete();
        }
    }

    private static void setField(
        final Object target,
        final String fieldName,
        final Object value,
        final Class<?> declaringClass
    ) throws Exception {
        final Field field = declaringClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void setTaskObject(
        final Task<?> task,
        final Object object
    ) throws Exception {
        final BehaviorTree<Object> behaviorTree = new BehaviorTree<>();
        behaviorTree.setObject(object);
        final Field treeField = Task.class.getDeclaredField("tree");
        treeField.setAccessible(true);
        treeField.set(task, behaviorTree);
    }
}
