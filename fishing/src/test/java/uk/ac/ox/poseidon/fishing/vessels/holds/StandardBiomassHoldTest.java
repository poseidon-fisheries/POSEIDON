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
    10.0,
    1.0
  );

  private static CategorisedCatch kg(final double kg) {
    return new CategorisedCatch(Map.of(CATEGORY, Bucket.of(SPECIES, kg)));
  }

  @Test
  void accumulatesWhatIsAddedUpToItsCapacityPlusTolerance() {
    hold.addContent(kg(6.0));
    hold.addContent(kg(5.0));

    assertThat(hold.getContent().getTotalBiomassInKg()).isEqualTo(11.0);
  }

  @Test
  void rejectsContentBeyondItsCapacityPlusTolerance() {
    hold.addContent(kg(6.0));

    assertThatThrownBy(() -> hold.addContent(kg(5.5)))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("only");
    assertThat(hold.getContent().getTotalBiomassInKg()).isEqualTo(6.0);
  }
}
