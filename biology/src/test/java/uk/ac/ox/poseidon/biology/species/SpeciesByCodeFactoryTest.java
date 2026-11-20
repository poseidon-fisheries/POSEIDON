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
import static uk.ac.ox.poseidon.biology.species.Factories.speciesByCode;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;

final class SpeciesByCodeFactoryTest {

  private static final Species COD_ADULT = new Species("COD", "adult", "Cod");
  private static final Species COD_JUVENILE = new Species(
    "COD",
    "juvenile",
    "Cod"
  );
  private static final Species HAD = new Species("HAD", null, "Haddock");
  private static final Species HKE = new Species("HKE", null, "Hake");

  @Test
  void selectedCodesKeepEveryLifeStageAndPreserveSpeciesListOrder() {
    final List<? extends Species> result = speciesByCode(
      s -> List.of("HAD", "COD", "COD", "UNKNOWN"),
      s -> List.of(COD_JUVENILE, HKE, HAD, COD_ADULT)
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result).hasSize(3);
    assertThat(result.get(0)).isSameAs(COD_JUVENILE);
    assertThat(result.get(1)).isSameAs(HAD);
    assertThat(result.get(2)).isSameAs(COD_ADULT);
  }

  @Test
  void emptyCodeSelectionKeepsNoSpecies() {
    final List<? extends Species> result = speciesByCode(
      s -> List.of(),
      s -> List.of(COD_ADULT, HAD)
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result).isEmpty();
  }

  @Test
  void unknownCodesKeepNoSpecies() {
    final List<? extends Species> result = speciesByCode(
      s -> List.of("UNKNOWN"),
      s -> List.of(COD_ADULT, HAD)
    ).get(Scope.GLOBAL_SCOPE);

    assertThat(result).isEmpty();
  }
}
