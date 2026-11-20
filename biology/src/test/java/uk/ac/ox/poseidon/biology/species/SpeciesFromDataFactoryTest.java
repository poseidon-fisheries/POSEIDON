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

package uk.ac.ox.poseidon.biology.species;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.biology.species.Factories.speciesFromData;

import java.util.List;
import org.junit.jupiter.api.Test;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import uk.ac.ox.poseidon.core.Scope;

final class SpeciesFromDataFactoryTest {

  @Test
  void readsCodesNamesAndLifeStagesFromTheConfiguredColumns() {
    final Table table = Table.create(
      "Species",
      StringColumn.create("code", "HAD", "COD"),
      StringColumn.create("display", "Haddock", "Cod"),
      StringColumn.create("stage", "juvenile", "adult")
    );

    final List<Species> result = speciesFromData(
      s -> table,
      "code",
      "display",
      "stage"
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result)
      .extracting(Species::getCode)
      .containsExactly("HAD", "COD");
    assertThat(result)
      .extracting(Species::getName)
      .containsExactly("Haddock", "Cod");
    assertThat(result)
      .extracting(Species::getLifeStage)
      .containsExactly("juvenile", "adult");
  }

  @Test
  void omittedLifeStageColumnProducesWholeSpecies() {
    final Table table = Table.create(
      "Species",
      StringColumn.create("code", "COD", "HAD"),
      StringColumn.create("display", "Cod", "Haddock")
    );

    final List<Species> result = speciesFromData(
      s -> table,
      "code",
      "display",
      null
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result)
      .extracting(Species::getCode)
      .containsExactly("COD", "HAD");
    assertThat(result)
      .extracting(Species::getName)
      .containsExactly("Cod", "Haddock");
    assertThat(result)
      .extracting(Species::getLifeStage)
      .containsExactly(null, null);
  }
}
