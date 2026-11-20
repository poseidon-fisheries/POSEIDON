/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.geography;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class EnvelopeTest {

  /**
   * Tests for the static intersects(Coordinate p1, Coordinate p2, Coordinate q)
   * method.
   */
  @Property
  public void testPropertyIntersects_PointInsideRectangle(
    @ForAll final double x1,
    @ForAll final double y1,
    @ForAll final double x2,
    @ForAll final double y2,
    @ForAll final double x,
    @ForAll final double y
  ) {
    final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
    final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
    final Coordinate q = new Coordinate(x, y);

    if (x >= p1.lon && x <= p2.lon && y >= p1.lat && y <= p2.lat) {
      // Point is inside the rectangle
      assertThat(Envelope.intersects(p1, p2, q)).isTrue();
    } else {
      // Point is outside the rectangle
      assertThat(Envelope.intersects(p1, p2, q)).isFalse();
    }
  }

  @Property
  public void testPropertyIntersects_PointOutsideRectangle(
    @ForAll final double x1,
    @ForAll final double y1,
    @ForAll final double x2,
    @ForAll final double y2,
    @ForAll final double x,
    @ForAll final double y
  ) {
    final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
    final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
    final Coordinate q = new Coordinate(x, y);

    if (x < p1.lon || x > p2.lon || y < p1.lat || y > p2.lat) {
      assertThat(Envelope.intersects(p1, p2, q)).isFalse();
    } else {
      assertThat(Envelope.intersects(p1, p2, q)).isTrue();
    }
  }

  @Test
  public void testIntersects_PointAtCorner() {
    final Coordinate p1 = new Coordinate(0.0, 0.0);
    final Coordinate p2 = new Coordinate(10.0, 10.0);
    final Coordinate q = new Coordinate(10.0, 10.0);

    assertThat(Envelope.intersects(p1, p2, q)).isTrue();
  }

  @Test
  public void testIntersects_PointInvertedRectangle() {
    final Coordinate p1 = new Coordinate(10.0, 10.0);
    final Coordinate p2 = new Coordinate(0.0, 0.0);
    final Coordinate q = new Coordinate(5.0, 5.0);

    assertThat(Envelope.intersects(p1, p2, q)).isTrue();
  }

  /**
   * Tests for the static intersects(Coordinate p1, Coordinate p2, Coordinate
   * q1, Coordinate q2) method.
   */
  @Property
  public void testPropertyIntersects_TwoOverlappingRectangles(
    @ForAll final double x1,
    @ForAll final double y1,
    @ForAll final double x2,
    @ForAll final double y2,
    @ForAll final double x3,
    @ForAll final double y3,
    @ForAll final double x4,
    @ForAll final double y4
  ) {
    final Coordinate p1 = new Coordinate(Math.min(x1, x2), Math.min(y1, y2));
    final Coordinate p2 = new Coordinate(Math.max(x1, x2), Math.max(y1, y2));
    final Coordinate q1 = new Coordinate(Math.min(x3, x4), Math.min(y3, y4));
    final Coordinate q2 = new Coordinate(Math.max(x3, x4), Math.max(y3, y4));

    final Envelope envelope1 = new Envelope(p1, p2);
    final Envelope envelope2 = new Envelope(q1, q2);

    final boolean intersects = envelope1.intersects(envelope2);
    final boolean expected = !(
      q2.lon < p1.lon ||
      q1.lon > p2.lon ||
      q2.lat < p1.lat ||
      q1.lat > p2.lat
    );

    assertThat(intersects).isEqualTo(expected);
  }

  @Test
  public void testIntersects_OneRectangleFullyInsideAnother() {
    final Coordinate p1 = new Coordinate(0.0, 0.0);
    final Coordinate p2 = new Coordinate(10.0, 10.0);
    final Coordinate q1 = new Coordinate(2.0, 2.0);
    final Coordinate q2 = new Coordinate(8.0, 8.0);

    assertThat(Envelope.intersects(p1, p2, q1, q2)).isTrue();
  }

  @Test
  public void testIntersects_RectanglesTouchAtEdge() {
    final Coordinate p1 = new Coordinate(0.0, 0.0);
    final Coordinate p2 = new Coordinate(10.0, 10.0);
    final Coordinate q1 = new Coordinate(10.0, 5.0);
    final Coordinate q2 = new Coordinate(15.0, 5.0);

    assertThat(Envelope.intersects(p1, p2, q1, q2)).isTrue();
  }

  @Test
  public void testIntersects_RectanglesTouchAtCorner() {
    final Coordinate p1 = new Coordinate(0.0, 0.0);
    final Coordinate p2 = new Coordinate(10.0, 10.0);
    final Coordinate q1 = new Coordinate(10.0, 10.0);
    final Coordinate q2 = new Coordinate(15.0, 15.0);

    assertThat(Envelope.intersects(p1, p2, q1, q2)).isTrue();
  }

  @Test
  public void testIntersects_InvertedRectanglesOverlap() {
    final Coordinate p1 = new Coordinate(10.0, 10.0);
    final Coordinate p2 = new Coordinate(0.0, 0.0);
    final Coordinate q1 = new Coordinate(15.0, 15.0);
    final Coordinate q2 = new Coordinate(5.0, 5.0);

    assertThat(Envelope.intersects(p1, p2, q1, q2)).isTrue();
  }

  @Test
  void emptyEnvelopeHasNoExtentOrCentre() {
    final var envelope = new Envelope();

    assertThat(envelope.isNull()).isTrue();
    assertThat(envelope.getWidth()).isZero();
    assertThat(envelope.getHeight()).isZero();
    assertThat(envelope.getArea()).isZero();
    assertThat(envelope.minExtent()).isZero();
    assertThat(envelope.maxExtent()).isZero();
    assertThat(envelope.centre()).isNull();
  }

  @Test
  void dimensionsAndCentreUseBothAxes() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.getWidth()).isEqualTo(6);
    assertThat(envelope.getHeight()).isEqualTo(4);
    assertThat(envelope.getArea()).isEqualTo(24);
    assertThat(envelope.minExtent()).isEqualTo(4);
    assertThat(envelope.maxExtent()).isEqualTo(6);
    assertThat(envelope.centre()).isEqualTo(new Coordinate(5, -1));
  }

  @Test
  void coordinateAndCopyConstructorsPreserveBounds() {
    final var point = new Coordinate(3, -2);
    final var envelope = new Envelope(
      new Coordinate(8, 1),
      new Coordinate(2, -3)
    );

    assertThat(new Envelope(point)).isEqualTo(new Envelope(3, 3, -2, -2));
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
    assertThat(new Envelope(envelope)).isEqualTo(new Envelope(2, 8, -3, 1));
    assertThat(new Envelope(point).isNull()).isFalse();
  }

  @Test
  void expansionUsesSeparateOffsetsAndLeavesOriginalUnchanged() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.expandBy(1, 2)).isEqualTo(new Envelope(1, 9, -5, 3));
    assertThat(envelope.expandBy(2)).isEqualTo(new Envelope(0, 10, -5, 3));
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
  }

  @Test
  void shrinkingToALineOrPointKeepsANonemptyEnvelope() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.expandBy(-3, 0)).isEqualTo(new Envelope(5, 5, -3, 1));
    assertThat(envelope.expandBy(0, -2)).isEqualTo(new Envelope(2, 8, -1, -1));
    assertThat(envelope.expandBy(-3, -2)).isEqualTo(new Envelope(5, 5, -1, -1));
    assertThat(envelope.expandBy(-3, -2).isNull()).isFalse();
  }

  @Test
  void shrinkingBeyondEitherExtentEmptiesTheEnvelope() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.expandBy(-4, 0).isNull()).isTrue();
    assertThat(envelope.expandBy(0, -3).isNull()).isTrue();
    assertThat(new Envelope().expandBy(5).isNull()).isTrue();
  }

  @Test
  void includingPointsExpandsOnlyTheNecessaryBounds() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.expandToInclude(-2, 4)).isEqualTo(
      new Envelope(-2, 8, -3, 4)
    );
    assertThat(envelope.expandToInclude(new Coordinate(10, -5))).isEqualTo(
      new Envelope(2, 10, -5, 1)
    );
    assertThat(envelope.expandToInclude(4, 0)).isEqualTo(
      new Envelope(2, 8, -3, 1)
    );
    assertThat(new Envelope().expandToInclude(4, -2)).isEqualTo(
      new Envelope(4, 4, -2, -2)
    );
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
  }

  @Test
  void includingEnvelopesCombinesTheirBounds() {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var other = new Envelope(-2, 4, 0, 5);

    assertThat(envelope.expandToInclude(other)).isEqualTo(
      new Envelope(-2, 8, -3, 5)
    );
    assertThat(envelope.expandToInclude(new Envelope())).isEqualTo(
      new Envelope(2, 8, -3, 1)
    );
    assertThat(new Envelope().expandToInclude(other)).isEqualTo(
      new Envelope(-2, 4, 0, 5)
    );
    assertThat(
      new Envelope().expandToInclude(new Envelope()).isNull()
    ).isTrue();
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
  }

  @Test
  void translationMovesBothBoundsOfEachAxis() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.translate(3, -2)).isEqualTo(
      new Envelope(5, 11, -5, -1)
    );
    assertThat(new Envelope().translate(3, -2).isNull()).isTrue();
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
  }

  @ParameterizedTest
  @CsvSource({
    "2, -3, true",
    "8, -3, true",
    "2, 1, true",
    "8, 1, true",
    "4, 0, true",
    "1, 0, false",
    "9, 0, false",
    "4, -4, false",
    "4, 2, false"
  })
  void pointPredicatesIncludeEveryBoundary(
    final double x,
    final double y,
    final boolean expected
  ) {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var point = new Coordinate(x, y);

    assertThat(envelope.covers(x, y)).isEqualTo(expected);
    assertThat(envelope.covers(point)).isEqualTo(expected);
    assertThat(envelope.contains(x, y)).isEqualTo(expected);
    assertThat(envelope.contains(point)).isEqualTo(expected);
    assertThat(envelope.intersects(x, y)).isEqualTo(expected);
    assertThat(envelope.intersects(point)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "2, 8, -3, 1, true",
    "3, 7, -2, 0, true",
    "1, 7, -2, 0, false",
    "3, 9, -2, 0, false",
    "3, 7, -4, 0, false",
    "3, 7, -2, 2, false"
  })
  void envelopeContainmentChecksEveryBoundary(
    final double minX,
    final double maxX,
    final double minY,
    final double maxY,
    final boolean expected
  ) {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var other = new Envelope(minX, maxX, minY, maxY);

    assertThat(envelope.covers(other)).isEqualTo(expected);
    assertThat(envelope.contains(other)).isEqualTo(expected);
  }

  @Test
  void emptyEnvelopesNeitherContainNorIntersect() {
    final var empty = new Envelope();
    final var envelope = new Envelope(-2, 2, -2, 2);
    final var point = new Coordinate(0, 0);

    assertThat(empty.covers(0, 0)).isFalse();
    assertThat(empty.covers(point)).isFalse();
    assertThat(empty.covers(envelope)).isFalse();
    assertThat(envelope.covers(empty)).isFalse();
    assertThat(empty.covers(empty)).isFalse();
    assertThat(empty.intersects(0, 0)).isFalse();
    assertThat(empty.intersects(point)).isFalse();
    assertThat(empty.intersects(envelope)).isFalse();
    assertThat(envelope.intersects(empty)).isFalse();
    assertThat(empty.intersects(empty)).isFalse();
  }

  @ParameterizedTest
  @CsvSource({
    "8, 10, -2, 0, true",
    "0, 2, -2, 0, true",
    "3, 7, 1, 3, true",
    "3, 7, -5, -3, true",
    "9, 10, -2, 0, false",
    "0, 1, -2, 0, false",
    "3, 7, 2, 3, false",
    "3, 7, -5, -4, false"
  })
  void envelopeIntersectionIncludesTouchingEdges(
    final double minX,
    final double maxX,
    final double minY,
    final double maxY,
    final boolean expected
  ) {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var other = new Envelope(minX, maxX, minY, maxY);

    assertThat(envelope.intersects(other)).isEqualTo(expected);
    assertThat(
      Envelope.intersects(
        new Coordinate(8, 1),
        new Coordinate(2, -3),
        new Coordinate(maxX, maxY),
        new Coordinate(minX, minY)
      )
    ).isEqualTo(expected);
  }

  @Test
  void intersectionReturnsOnlyTheSharedRegion() {
    final var envelope = new Envelope(2, 8, -3, 1);

    assertThat(envelope.intersection(new Envelope(5, 10, -5, 0))).isEqualTo(
      new Envelope(5, 8, -3, 0)
    );
    assertThat(envelope.intersection(new Envelope(8, 10, 1, 3))).isEqualTo(
      new Envelope(8, 8, 1, 1)
    );
    assertThat(
      envelope.intersection(new Envelope(9, 10, -2, 0)).isNull()
    ).isTrue();
    assertThat(envelope.intersection(new Envelope()).isNull()).isTrue();
    assertThat(new Envelope().intersection(envelope).isNull()).isTrue();
  }

  @ParameterizedTest
  @CsvSource({
    "3, 5, -2, 0, 0",
    "8, 10, 1, 3, 0",
    "11, 13, -2, 0, 3",
    "-3, -1, -2, 0, 3",
    "3, 7, 5, 7, 4",
    "3, 7, -9, -7, 4",
    "11, 13, 5, 7, 5",
    "-3, -1, -9, -7, 5"
  })
  void distanceUsesTheShortestHorizontalVerticalOrDiagonalGap(
    final double minX,
    final double maxX,
    final double minY,
    final double maxY,
    final double expected
  ) {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var other = new Envelope(minX, maxX, minY, maxY);

    assertThat(envelope.distance(other)).isEqualTo(expected);
    assertThat(other.distance(envelope)).isEqualTo(expected);
  }

  @Test
  void jtsConversionsPreserveBoundsWithoutSharingMutableState() {
    final var jts = new org.locationtech.jts.geom.Envelope(2, 8, -3, 1);
    final var envelope = Envelope.fromJTS(jts);

    jts.expandBy(5);
    final var converted = envelope.toJTS();

    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
    assertThat(converted.getMinX()).isEqualTo(2);
    assertThat(converted.getMaxX()).isEqualTo(8);
    assertThat(converted.getMinY()).isEqualTo(-3);
    assertThat(converted.getMaxY()).isEqualTo(1);
    converted.expandBy(3);
    assertThat(envelope).isEqualTo(new Envelope(2, 8, -3, 1));
    assertThat(Envelope.fromJTS(null).isNull()).isTrue();
  }

  @SuppressWarnings("deprecation")
  @ParameterizedTest
  @CsvSource({ "4, 0, true", "9, 0, false" })
  void deprecatedOverlapAliasesPreserveIntersectionBehaviour(
    final double x,
    final double y,
    final boolean expected
  ) {
    final var envelope = new Envelope(2, 8, -3, 1);
    final var point = new Coordinate(x, y);
    final var other = new Envelope(point);

    assertThat(envelope.overlaps(x, y)).isEqualTo(expected);
    assertThat(envelope.overlaps(point)).isEqualTo(expected);
    assertThat(envelope.overlaps(other)).isEqualTo(expected);
  }
}
