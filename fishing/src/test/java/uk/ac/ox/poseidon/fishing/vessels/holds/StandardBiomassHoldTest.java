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

package uk.ac.ox.poseidon.fishing.vessels.holds;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.fishing.catches.CatchCategory;
import uk.ac.ox.poseidon.fishing.catches.CategorisedCatch;

final class StandardBiomassHoldTest {

  private static final CatchCategory CATEGORY = new CatchCategory("C");
  private static final Species SPECIES = new Species("S", null, null);

  private final StandardBiomassHold hold = new StandardBiomassHold(
    bucket -> new CategorisedCatch(Map.of(CATEGORY, bucket)),
    10.0
  );

  private static CategorisedCatch kg(final double kg) {
    return new CategorisedCatch(Map.of(CATEGORY, Bucket.of(SPECIES, kg)));
  }

  @Test
  void acceptsRoundingWithinTheRelativeMargin() {
    hold.addContent(kg(6.0));
    hold.addContent(kg(4.000000005));

    assertThat(hold.getContent().getTotalBiomassInKg()).isEqualTo(10.000000005);
  }

  @Test
  void rejectsContentBeyondItsCapacityAndReportsTheCause() {
    hold.addContent(kg(6.0));

    assertThatThrownBy(() -> hold.addContent(kg(5.5)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("5.5 kg")
      .hasMessageContaining("6.0 kg")
      .hasMessageContaining("10.0 kg")
      .hasMessageContaining("sorting");
    assertThat(hold.getContent().getTotalBiomassInKg()).isEqualTo(6.0);
  }

  @Test
  void rejectsContentBeyondRelativeMarginWithoutChangingContent() {
    hold.addContent(kg(6.0));

    assertThatThrownBy(() -> hold.addContent(kg(4.000001))).isInstanceOf(
      IllegalStateException.class
    );
    assertThat(hold.getContent().getTotalBiomassInKg()).isEqualTo(6.0);
  }

  @Test
  void aZeroCapacityHoldRejectsAnyPositiveCatch() {
    final Hold emptyHold = new StandardBiomassHold(
      bucket -> new CategorisedCatch(Map.of(CATEGORY, bucket)),
      0
    );

    assertThatThrownBy(() -> emptyHold.addContent(kg(0.000001))).isInstanceOf(
      IllegalStateException.class
    );
    assertThat(emptyHold.isEmpty()).isTrue();
  }

  @Test
  void anInfiniteHoldAcceptsCatchWithoutAFiniteLimit() {
    final Hold infiniteHold = new InfiniteBiomassHold(bucket ->
      new CategorisedCatch(Map.of(CATEGORY, bucket))
    );

    infiniteHold.addContent(kg(1e100));
    infiniteHold.addContent(Bucket.of(SPECIES, 1e100));

    assertThat(infiniteHold.getContent().getTotalBiomassInKg()).isEqualTo(
      2e100
    );
  }
}
