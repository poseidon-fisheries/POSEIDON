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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;

final class BiomassBucketIndexedTest {

  private final Species a = new Species("A", null, "A");
  private final Species b = new Species("B", null, "B");
  private final Species c = new Species("C", null, "C");
  private final SpeciesIndex speciesIndex = SpeciesIndex.of(a, b, c);

  @Test
  void mappingWithSharedIndexUsesBiomassAndPositionAndSkipsAbsentSpecies() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 0, 30 },
      speciesIndex
    );
    final List<Integer> visited = new ArrayList<>();

    final Bucket mapped = bucket.mapWithIndex(bucket, (value, index) -> {
      visited.add(index);
      return value + index;
    });

    assertThat(visited).containsExactly(0, 2);
    assertThat(mapped.getKg(a)).isEqualTo(10);
    assertThat(mapped.getKg(b)).isZero();
    assertThat(mapped.getKg(c)).isEqualTo(32);
    assertThat(mapped.getSpecies()).containsExactlyInAnyOrder(a, c);
  }

  @Test
  void mappingWithSharedIndexDropsNaNAndZeroButKeepsPositiveResults() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 30 },
      speciesIndex
    );
    final double[] results = { Double.NaN, 0, 7 };

    final Bucket mapped = bucket.mapWithIndex(
      bucket,
      (value, index) -> results[index]
    );

    assertThat(mapped.getSpecies()).containsExactly(c);
    assertThat(mapped.getKg(a)).isZero();
    assertThat(mapped.getKg(b)).isZero();
    assertThat(mapped.getKg(c)).isEqualTo(7);
  }

  @Test
  void mappingWithSharedIndexReturnsEmptyWhenAllResultsAreAbsent() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 30 },
      speciesIndex
    );

    assertThat(bucket.mapWithIndex(bucket, (value, index) -> 0)).isSameAs(
      Bucket.empty()
    );
    assertThat(
      bucket.mapWithIndex(bucket, (value, index) -> Double.NaN)
    ).isSameAs(Bucket.empty());
  }

  @Test
  void mappingWithSharedIndexRejectsNegativeResults() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 30 },
      speciesIndex
    );

    assertThatIllegalArgumentException().isThrownBy(() ->
      bucket.mapWithIndex(bucket, (value, index) -> -2)
    );
  }

  @Test
  void mappingWithDifferentIndexUsesOtherPositionsAndDropsMissingSpecies() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 30 },
      speciesIndex
    );
    final BiomassBucket other = BiomassBucket.create(
      new double[] { 1, 1 },
      SpeciesIndex.of(b, c)
    );
    final List<Integer> visited = new ArrayList<>();

    final Bucket mapped = bucket.mapWithIndex(other, (value, index) -> {
      visited.add(index);
      return value + index;
    });

    assertThat(visited).containsExactly(0, 1);
    assertThat(mapped.getSpecies()).containsExactlyInAnyOrder(b, c);
    assertThat(mapped.getKg(a)).isZero();
    assertThat(mapped.getKg(b)).isEqualTo(20);
    assertThat(mapped.getKg(c)).isEqualTo(31);
  }

  @Test
  void iterationWithSharedIndexVisitsOnlyPresentSpecies() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 0, 30 },
      speciesIndex
    );
    final Map<Integer, Double> visited = new HashMap<>();
    final Map<Species, Double> missing = new HashMap<>();

    bucket.forEachWithIndex(
      bucket,
      (value, index) -> visited.put(index, value),
      missing::put
    );

    assertThat(visited).containsOnly(Map.entry(0, 10.0), Map.entry(2, 30.0));
    assertThat(missing).isEmpty();
  }

  @Test
  void iterationWithDifferentIndexReportsMissingSpeciesAndUsesOtherPositions() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 30 },
      speciesIndex
    );
    final BiomassBucket other = BiomassBucket.create(
      new double[] { 1, 1 },
      SpeciesIndex.of(b, c)
    );
    final Map<Integer, Double> visited = new HashMap<>();
    final Map<Species, Double> missing = new HashMap<>();

    bucket.forEachWithIndex(
      other,
      (value, index) -> visited.put(index, value),
      missing::put
    );

    assertThat(visited).containsOnly(Map.entry(0, 20.0), Map.entry(1, 30.0));
    assertThat(missing).containsOnly(Map.entry(a, 10.0));
  }

  @Test
  void indexedAccessReturnsStoredBiomassIncludingAbsentSlots() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 0, 30 },
      speciesIndex
    );

    assertThat(bucket.getDouble(0)).isEqualTo(10);
    assertThat(bucket.getDouble(1)).isZero();
    assertThat(bucket.getDouble(c)).isEqualTo(30);
  }

  @Test
  void creationRejectsArraysShorterOrLongerThanTheSpeciesIndex() {
    assertThatIllegalArgumentException().isThrownBy(() ->
      BiomassBucket.create(new double[] { 10, 20 }, speciesIndex)
    );
    assertThatIllegalArgumentException().isThrownBy(() ->
      BiomassBucket.create(new double[] { 10, 20, 30, 40 }, speciesIndex)
    );
  }

  @Test
  void subtractionOnASharedIndexKeepsRemaindersAndDropsExhaustedSpecies() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 0 },
      speciesIndex
    );
    final BiomassBucket removed = BiomassBucket.create(
      new double[] { 3, 20, 0 },
      speciesIndex
    );

    final Bucket remainder = bucket.subtract(removed);

    assertThat(remainder.getKg(a)).isEqualTo(7);
    assertThat(remainder.getSpecies()).containsExactly(a);
    assertThat(bucket.getKg(a)).isEqualTo(10);
    assertThat(bucket.getKg(b)).isEqualTo(20);
  }

  @Test
  void subtractingAllBiomassOnASharedIndexReturnsTheEmptyBucket() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 0 },
      speciesIndex
    );

    assertThat(bucket.subtract(bucket)).isSameAs(Bucket.empty());
  }

  @Test
  void subtractionRejectsOverdrawingContentFromAnotherBucketRepresentation() {
    final BiomassBucket bucket = BiomassBucket.create(
      new double[] { 10, 20, 0 },
      speciesIndex
    );
    final Bucket removed = Bucket.of(a, 11);

    assertThatIllegalArgumentException().isThrownBy(() ->
      bucket.subtract(removed)
    );
  }
}
