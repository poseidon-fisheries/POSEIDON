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

package uk.ac.ox.poseidon.fishing.tasks.branches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;

class FallbackTaskFactoryTest {

  @Test
  void failureTriesTheNextChildAndSuccessStops() {
    final StubTask first = spy(new StubTask(Task.Status.FAILED));
    final StubTask second = spy(new StubTask(Task.Status.SUCCEEDED));
    final StubTask third = spy(new StubTask(Task.Status.SUCCEEDED));
    final BehaviorTree<Vessel> tree = tree(first, second, third);
    tree.step();
    assertThat(tree.getStatus()).isEqualTo(Task.Status.SUCCEEDED);
    verify(first).execute();
    verify(second).execute();
    verify(third, never()).execute();
  }

  @Test
  void runningChildContinuesBeforeTryingTheNextChild() {
    final StubTask first = spy(new StubTask(Task.Status.RUNNING));
    final StubTask second = spy(new StubTask(Task.Status.SUCCEEDED));
    final BehaviorTree<Vessel> tree = tree(first, second);
    tree.step();
    tree.step();
    assertThat(tree.getStatus()).isEqualTo(Task.Status.RUNNING);
    verify(first, times(2)).execute();
    verify(second, never()).execute();
    first.statusToReturn = Task.Status.FAILED;
    tree.step();
    assertThat(tree.getStatus()).isEqualTo(Task.Status.SUCCEEDED);
    verify(second).execute();
  }

  @Test
  void allFailuresFail() {
    final BehaviorTree<Vessel> tree = tree(
      new StubTask(Task.Status.FAILED),
      new StubTask(Task.Status.FAILED)
    );
    tree.step();
    assertThat(tree.getStatus()).isEqualTo(Task.Status.FAILED);
  }

  private static BehaviorTree<Vessel> tree(final StubTask... children) {
    final FallbackTaskFactory factory = new FallbackTaskFactory(
      java.util.Arrays.stream(children)
        .<
          uk.ac.ox.poseidon.core.Factory<VesselComponentScope, Task<Vessel>>
        >map(child -> _ -> child)
        .toList()
    );
    return new BehaviorTree<>(
      factory.newInstance(mock(VesselComponentScope.class)),
      mock(Vessel.class)
    );
  }

  private static class StubTask extends LeafTask<Vessel> {

    private Status statusToReturn;

    StubTask(final Status statusToReturn) {
      this.statusToReturn = statusToReturn;
    }

    @Override
    public Status execute() {
      return statusToReturn;
    }

    @Override
    protected Task<Vessel> copyTo(final Task<Vessel> task) {
      return task;
    }
  }
}
