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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static uk.ac.ox.poseidon.biology.species.Factories.species;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;

final class SpeciesTest {

  @Test
  void codeOnlyFactorySuppliesADisplayNameWithoutALifeStage() {
    final Species result = species("COD").get(Scope.GLOBAL_SCOPE);

    assertThat(result.getCode()).isEqualTo("COD");
    assertThat(result.getName()).isEqualTo("Species COD");
    assertThat(result.getLifeStage()).isNull();
  }

  @Test
  void fullFactoryKeepsNameAndLifeStageInTheirCorrectFields() {
    final Species result = species("COD", "Cod", "adult").get(
      Scope.GLOBAL_SCOPE
    );

    assertThat(result.getCode()).isEqualTo("COD");
    assertThat(result.getName()).isEqualTo("Cod");
    assertThat(result.getLifeStage()).isEqualTo("adult");
  }

  @Test
  void missingDisplayNameDefaultsToTheSpeciesCode() {
    final Species result = species("COD", null, null).get(Scope.GLOBAL_SCOPE);

    assertThat(result.getName()).isEqualTo("Species COD");
    assertThat(result.getLifeStage()).isNull();
  }

  @Test
  void missingCodesAreRejectedByBothTheSpeciesAndItsFactory() {
    assertThatNullPointerException().isThrownBy(() ->
      new Species(null, null, "Cod")
    );
    assertThatNullPointerException().isThrownBy(() ->
      species(null).get(Scope.GLOBAL_SCOPE)
    );
  }

  @Test
  void blankNamesAndLifeStagesAreNormalisedToNull() {
    final Species result = new Species("COD", "  ", " ");

    assertThat(result.getName()).isNull();
    assertThat(result.getLifeStage()).isNull();
  }

  @Test
  void wholeSpeciesCoversItsLifeStagesButDoesNotCoverOtherCodes() {
    final Species cod = new Species("COD", null, "Cod");

    assertThat(cod.covers(new Species("COD", null, "Different name"))).isTrue();
    assertThat(cod.covers(new Species("COD", "adult", "Cod"))).isTrue();
    assertThat(cod.covers(new Species("HAD", null, "Haddock"))).isFalse();
    assertThat(cod.covers(new Species("HAD", "adult", "Haddock"))).isFalse();
  }

  @Test
  void lifeStageCoversOnlyTheSameCodeAndLifeStage() {
    final Species adultCod = new Species("COD", "adult", "Cod");

    assertThat(
      adultCod.covers(new Species("COD", "adult", "Different name"))
    ).isTrue();
    assertThat(
      adultCod.covers(new Species("COD", "juvenile", "Cod"))
    ).isFalse();
    assertThat(adultCod.covers(new Species("COD", null, "Cod"))).isFalse();
    assertThat(
      adultCod.covers(new Species("HAD", "adult", "Haddock"))
    ).isFalse();
  }
}
