/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import lombok.Data;

/**
 * An immutable bounding box, equivalent to
 * {@code org.locationtech.jts.geom.Envelope} but safe for concurrency and use
 * as a hash key. Built via
 * {@link Factories#envelope(double, double, double, double)}. A "null" envelope
 * (one where {@code maxX < minX} or {@code maxY < minY}, as returned by the
 * no-arg constructor) represents an empty region; most methods special-case it
 * (e.g. {@link #getWidth()} returns {@code 0}, most predicates return
 * {@code false}).
 */
@Data
public final class Envelope {

  private final double minX;
  private final double maxX;
  private final double minY;
  private final double maxY;

  /**
   * Constructs a null Envelope, i.e. minx > maxx, miny > maxy.
   */
  public Envelope() {
    // Same "setToNull" logic:
    this.minX = 0.0;
    this.maxX = -1.0;
    this.minY = 0.0;
    this.maxY = -1.0;
  }

  /**
   * Constructs an Envelope by ordering (x1, x2) and (y1, y2) so
   * {@code min <= max}.
   */
  public Envelope(
    final double x1,
    final double x2,
    final double y1,
    final double y2
  ) {
    if (x1 < x2) {
      this.minX = x1;
      this.maxX = x2;
    } else {
      this.minX = x2;
      this.maxX = x1;
    }
    if (y1 < y2) {
      this.minY = y1;
      this.maxY = y2;
    } else {
      this.minY = y2;
      this.maxY = y1;
    }
  }

  /**
   * Constructs an Envelope from two coordinates.
   */
  public Envelope(final Coordinate p1, final Coordinate p2) {
    this(p1.lon, p2.lon, p1.lat, p2.lat);
  }

  /**
   * Constructs an Envelope covering a single point.
   */
  public Envelope(final Coordinate p) {
    this(p.lon, p.lon, p.lat, p.lat);
  }

  /**
   * Copy constructor.
   */
  public Envelope(final Envelope env) {
    this(env.minX, env.maxX, env.minY, env.maxY);
  }

  /**
   * @return whether the envelope defined by {@code p1}/{@code p2} intersects
   *         the point {@code q}
   */
  public static boolean intersects(
    final Coordinate p1,
    final Coordinate p2,
    final Coordinate q
  ) {
    return (
      q.lon >= Math.min(p1.lon, p2.lon) &&
      q.lon <= Math.max(p1.lon, p2.lon) &&
      q.lat >= Math.min(p1.lat, p2.lat) &&
      q.lat <= Math.max(p1.lat, p2.lat)
    );
  }

  /**
   * @return whether the envelope defined by {@code p1}/{@code p2} intersects
   *         the envelope defined by {@code q1}/{@code q2}
   */
  public static boolean intersects(
    final Coordinate p1,
    final Coordinate p2,
    final Coordinate q1,
    final Coordinate q2
  ) {
    return (
      overlap(p1.lon, p2.lon, q1.lon, q2.lon) &&
      overlap(p1.lat, p2.lat, q1.lat, q2.lat)
    );
  }

  /**
   * @return whether the interval between {@code p1} and {@code p2} overlaps
   *         the one between {@code q1} and {@code q2}
   */
  private static boolean overlap(
    final double p1,
    final double p2,
    final double q1,
    final double q2
  ) {
    return (
      Math.min(p1, p2) <= Math.max(q1, q2) &&
      Math.max(p1, p2) >= Math.min(q1, q2)
    );
  }

  /**
   * Creates an immutable Envelope from a JTS Envelope.
   */
  public static Envelope fromJTS(
    final org.locationtech.jts.geom.Envelope jtsEnv
  ) {
    return jtsEnv == null
      ? new Envelope() // null envelope
      : new Envelope(
          jtsEnv.getMinX(),
          jtsEnv.getMaxX(),
          jtsEnv.getMinY(),
          jtsEnv.getMaxY()
        );
  }

  /**
   * @return whether this envelope is empty (i.e. {@code maxX < minX} or
   *         {@code maxY < minY})
   */
  public boolean isNull() {
    return maxX < minX || maxY < minY;
  }

  /**
   * @return {@code maxX - minX}, or {@code 0} if this envelope is
   *         {@link #isNull() null}
   */
  public double getWidth() {
    return isNull() ? 0.0 : maxX - minX;
  }

  /**
   * @return {@code maxY - minY}, or {@code 0} if this envelope is
   *         {@link #isNull() null}
   */
  public double getHeight() {
    return isNull() ? 0.0 : maxY - minY;
  }

  /** @return {@link #getWidth()} times {@link #getHeight()} */
  public double getArea() {
    return getWidth() * getHeight();
  }

  /**
   * @return the smaller of {@link #getWidth()} and {@link #getHeight()}, or
   *         {@code 0} if this envelope is {@link #isNull() null}
   */
  public double minExtent() {
    return Math.min(getWidth(), getHeight());
  }

  /**
   * @return the larger of {@link #getWidth()} and {@link #getHeight()}, or
   *         {@code 0} if this envelope is {@link #isNull() null}
   */
  public double maxExtent() {
    return Math.max(getWidth(), getHeight());
  }

  /**
   * Returns a new Envelope expanded by the same distance in both x and y.
   */
  public Envelope expandBy(final double distance) {
    return expandBy(distance, distance);
  }

  /**
   * Returns a new Envelope expanded by deltaX in x direction and deltaY in y
   * direction.
   */
  public Envelope expandBy(final double deltaX, final double deltaY) {
    final double newMinX = minX - deltaX;
    final double newMaxX = maxX + deltaX;
    final double newMinY = minY - deltaY;
    final double newMaxY = maxY + deltaY;
    final Envelope expanded;
    if (isNull()) {
      expanded = this;
    } else if (newMinX > newMaxX || newMinY > newMaxY) {
      // This yields a null envelope
      expanded = new Envelope();
    } else {
      expanded = new Envelope(newMinX, newMaxX, newMinY, newMaxY);
    }
    return expanded;
  }

  /**
   * Returns a new Envelope expanded to include (x, y).
   */
  public Envelope expandToInclude(final double x, final double y) {
    return isNull()
      ? new Envelope(x, x, y, y)
      : new Envelope(
          Math.min(minX, x),
          Math.max(maxX, x),
          Math.min(minY, y),
          Math.max(maxY, y)
        );
  }

  /**
   * Returns a new Envelope expanded to include the coordinate p.
   */
  public Envelope expandToInclude(final Coordinate p) {
    return expandToInclude(p.lon, p.lat);
  }

  /**
   * Returns a new Envelope expanded to include another Envelope.
   */
  public Envelope expandToInclude(final Envelope other) {
    final Envelope expanded;
    if (other.isNull()) {
      expanded = this;
    } else if (this.isNull()) {
      expanded = new Envelope(other);
    } else {
      expanded = new Envelope(
        Math.min(this.minX, other.minX),
        Math.max(this.maxX, other.maxX),
        Math.min(this.minY, other.minY),
        Math.max(this.maxY, other.maxY)
      );
    }
    return expanded;
  }

  /**
   * Returns a new Envelope translated by transX, transY.
   */
  public Envelope translate(final double transX, final double transY) {
    return isNull()
      ? this
      : new Envelope(
          minX + transX,
          maxX + transX,
          minY + transY,
          maxY + transY
        );
  }

  /**
   * @return the midpoint of this envelope, or {@code null} if it is
   *         {@link #isNull() null}
   */
  public Coordinate centre() {
    return isNull()
      ? null
      : new Coordinate((minX + maxX) / 2.0, (minY + maxY) / 2.0);
  }

  /**
   * @return the overlapping region of this envelope and {@code env}, or a
   *         {@link #isNull() null} envelope if they don't overlap
   */
  public Envelope intersection(final Envelope env) {
    // Two null envelopes don't intersect
    return this.intersects(env)
      ? new Envelope(
          Math.max(this.minX, env.minX),
          Math.min(this.maxX, env.maxX),
          Math.max(this.minY, env.minY),
          Math.min(this.maxY, env.maxY)
        )
      : new Envelope();
  }

  /**
   * @return whether this envelope and {@code other} overlap (touching counts as
   *         overlapping)
   */
  public boolean intersects(final Envelope other) {
    return (
      !this.isNull() &&
      !other.isNull() &&
      other.minX <= this.maxX &&
      other.maxX >= this.minX &&
      other.minY <= this.maxY &&
      other.maxY >= this.minY
    );
  }

  /** @deprecated use {@link #intersects(Envelope)} */
  @Deprecated
  public boolean overlaps(final Envelope other) {
    return intersects(other);
  }

  /** @return whether this envelope contains or touches the point {@code p} */
  public boolean intersects(final Coordinate p) {
    return intersects(p.lon, p.lat);
  }

  /** @deprecated use {@link #intersects(Coordinate)} */
  @Deprecated
  public boolean overlaps(final Coordinate p) {
    return intersects(p);
  }

  /**
   * @return whether this envelope contains or touches the point {@code (x, y)}
   */
  public boolean intersects(final double x, final double y) {
    return !isNull() && x <= maxX && x >= minX && y <= maxY && y >= minY;
  }

  /** @deprecated use {@link #intersects(double, double)} */
  @Deprecated
  public boolean overlaps(final double x, final double y) {
    return intersects(x, y);
  }

  /**
   * @return whether this envelope fully covers {@code other} (alias for
   *         {@link #covers(Envelope)})
   */
  public boolean contains(final Envelope other) {
    return covers(other);
  }

  /**
   * @return whether this envelope covers the point {@code p} (alias for
   *         {@link #covers(Coordinate)})
   */
  public boolean contains(final Coordinate p) {
    return covers(p);
  }

  /**
   * @return whether this envelope covers the point {@code (x, y)} (alias for
   *         {@link #covers(double, double)})
   */
  public boolean contains(final double x, final double y) {
    return covers(x, y);
  }

  /**
   * @return whether this envelope covers the point {@code (x, y)}; unlike
   *         {@link #intersects(double, double)}, a {@link #isNull() null}
   *         envelope covers nothing
   */
  public boolean covers(final double x, final double y) {
    return !isNull() && x >= minX && x <= maxX && y >= minY && y <= maxY;
  }

  /** @return whether this envelope covers the point {@code p} */
  public boolean covers(final Coordinate p) {
    return covers(p.lon, p.lat);
  }

  /** @return whether this envelope fully covers {@code other} */
  public boolean covers(final Envelope other) {
    return (
      !this.isNull() &&
      !other.isNull() &&
      other.minX >= this.minX &&
      other.maxX <= this.maxX &&
      other.minY >= this.minY &&
      other.maxY <= this.maxY
    );
  }

  /**
   * @return the shortest distance between this envelope and {@code env}, or
   *         {@code 0} if they overlap
   */
  public double distance(final Envelope env) {
    final double dx = gap(this.minX, this.maxX, env.minX, env.maxX);
    final double dy = gap(this.minY, this.maxY, env.minY, env.maxY);
    // Along one axis, the distance is the gap itself, without rounding
    return dx == 0.0 || dy == 0.0 ? dx + dy : Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * @return the gap between the interval from {@code minA} to {@code maxA}
   *         and the one from {@code minB} to {@code maxB}, or {@code 0} if
   *         they overlap
   */
  private static double gap(
    final double minA,
    final double maxA,
    final double minB,
    final double maxB
  ) {
    return Math.max(0.0, Math.max(minB - maxA, minA - maxB));
  }

  /**
   * Converts this immutable Envelope to a JTS Envelope.
   */
  public org.locationtech.jts.geom.Envelope toJTS() {
    return new org.locationtech.jts.geom.Envelope(minX, maxX, minY, maxY);
  }
}
