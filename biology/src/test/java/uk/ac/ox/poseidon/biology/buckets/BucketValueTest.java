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

package uk.ac.ox.poseidon.biology.buckets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

final class BucketValueTest {

  private static final Species COD = new Species("COD", null, "Cod");
  private static final Species HAD = new Species("HAD", null, "Haddock");

  @Test
  void contentBucketReportsTheCombinedMassInKilograms() {
    final Bucket bucket = ContentBucket.ofBiomassMap(
      Map.of(COD, 3.0, HAD, 8.0)
    );

    assertThat(bucket.getTotalBiomassInKg()).isEqualTo(11);
  }

  @Test
  void bucketDoesNotEqualNullOrAnUnrelatedObject() {
    final Bucket bucket = Bucket.of(COD, 3);

    assertThat(Bucket.contentEquals(bucket, null)).isFalse();
    assertThat(Bucket.contentEquals(bucket, "COD")).isFalse();
  }

  @Test
  void singleSpeciesFactoryRejectsNegativeBiomass() {
    assertThatIllegalArgumentException().isThrownBy(() -> Bucket.of(COD, -1));
  }

  @Test
  void contentBucketDropsZeroMassEntries() {
    final Bucket bucket = ContentBucket.ofContentMap(
      Map.of(COD, Biomass.ZERO, HAD, Biomass.ofKg(8))
    );

    assertThat(bucket.getSpecies()).containsExactly(HAD);
    assertThat(bucket.getContent(COD)).isEmpty();
    assertThat(bucket.getTotalBiomassInKg()).isEqualTo(8);
  }
}
