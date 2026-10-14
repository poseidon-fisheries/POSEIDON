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

package uk.ac.ox.poseidon.regulations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.alwaysFalse;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.alwaysTrue;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.regulations.Factories.allOf;
import static uk.ac.ox.poseidon.regulations.Factories.forbiddenIf;
import static uk.ac.ox.poseidon.regulations.Factories.permittedIf;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;

class AllOfRegulationsTest {

  @SuppressWarnings("unchecked")
  private final Action<Object> action = mock(Action.class);

  private static final Regulations<Action<Object>> PERMITTING = a -> true;
  private static final Regulations<Action<Object>> FORBIDDING = a -> false;

  @Test
  void permitsAnActionOnlyWhenEveryRulePermitsIt() {
    assertThat(
      new AllOfRegulations<>(List.of(PERMITTING, PERMITTING)).isPermitted(
        action
      )
    ).isTrue();
    assertThat(
      new AllOfRegulations<>(List.of(PERMITTING, FORBIDDING)).isPermitted(
        action
      )
    ).isFalse();
    assertThat(
      new AllOfRegulations<>(List.of(FORBIDDING, PERMITTING)).isPermitted(
        action
      )
    ).isFalse();
  }

  @Test
  void permitsEveryActionWhenThereAreNoRules() {
    assertThat(
      new AllOfRegulations<Action<Object>>(List.of()).isPermitted(action)
    ).isTrue();
  }

  @Test
  void stopsAtTheFirstRuleThatForbids() {
    final List<String> asked = new ArrayList<>();
    final Regulations<Action<Object>> first = a -> asked.add("first") && false;
    final Regulations<Action<Object>> second = a -> asked.add("second");

    new AllOfRegulations<>(List.of(first, second)).isPermitted(action);

    assertThat(asked).containsExactly("first");
  }

  @Test
  void allOfRulesGivesRules() {
    final AllOfRegulationsFactory<Scope, Action<Object>> permitting = allOf(
      permittedIf(alwaysTrue()),
      forbiddenIf(alwaysFalse())
    );
    final AllOfRegulationsFactory<Scope, Action<Object>> forbidding = allOf(
      permittedIf(alwaysTrue()),
      forbiddenIf(alwaysTrue())
    );

    assertThat(permitting.get(GLOBAL_SCOPE).isPermitted(action)).isTrue();
    assertThat(forbidding.get(GLOBAL_SCOPE).isPermitted(action)).isFalse();
  }
}
