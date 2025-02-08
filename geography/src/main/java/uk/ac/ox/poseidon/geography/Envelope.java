/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography;

import lombok.Data;

/**
 * Immutable Envelope equivalent to com.vividsolutions.jts.geom.Envelope
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
     * Constructs an Envelope by ordering (x1, x2) and (y1, y2) so min <= max.
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
    public Envelope(
        final Coordinate p1,
        final Coordinate p2
    ) {
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

    public static boolean intersects(
        final Coordinate p1,
        final Coordinate p2,
        final Coordinate q
    ) {
        return q.lon >= Math.min(p1.lon, p2.lon)
            && q.lon <= Math.max(p1.lon, p2.lon)
            && q.lat >= Math.min(p1.lat, p2.lat)
            && q.lat <= Math.max(p1.lat, p2.lat);
    }

    public static boolean intersects(
        final Coordinate p1,
        final Coordinate p2,
        final Coordinate q1,
        final Coordinate q2
    ) {
        double minQ = Math.min(q1.lon, q2.lon);
        double maxQ = Math.max(q1.lon, q2.lon);
        double minP = Math.min(p1.lon, p2.lon);
        double maxP = Math.max(p1.lon, p2.lon);
        if (minP > maxQ) return false;
        if (maxP < minQ) return false;

        minQ = Math.min(q1.lat, q2.lat);
        maxQ = Math.max(q1.lat, q2.lat);
        minP = Math.min(p1.lat, p2.lat);
        maxP = Math.max(p1.lat, p2.lat);

        if (minP > maxQ) return false;
        return maxP >= minQ;
    }

    /**
     * Creates an immutable Envelope from a JTS Envelope.
     */
    public static Envelope fromJTS(final com.vividsolutions.jts.geom.Envelope jtsEnv) {
        if (jtsEnv == null) {
            return new Envelope(); // null envelope
        }
        return new Envelope(
            jtsEnv.getMinX(),
            jtsEnv.getMaxX(),
            jtsEnv.getMinY(),
            jtsEnv.getMaxY()
        );
    }

    public boolean isNull() {
        return (maxX < minX) || (maxY < minY);
    }

    public double getWidth() {
        return isNull() ? 0.0 : (maxX - minX);
    }

    public double getHeight() {
        return isNull() ? 0.0 : (maxY - minY);
    }

    public double getArea() {
        return getWidth() * getHeight();
    }

    public double minExtent() {
        if (isNull()) return 0.0;
        return Math.min(getWidth(), getHeight());
    }

    public double maxExtent() {
        if (isNull()) return 0.0;
        return Math.max(getWidth(), getHeight());
    }

    /**
     * Returns a new Envelope expanded by the same distance in both x and y.
     */
    public Envelope expandBy(final double distance) {
        return expandBy(distance, distance);
    }

    /**
     * Returns a new Envelope expanded by deltaX in x direction and deltaY in y direction.
     */
    public Envelope expandBy(
        final double deltaX,
        final double deltaY
    ) {
        if (isNull()) {
            return this;
        }
        final double newMinX = minX - deltaX;
        final double newMaxX = maxX + deltaX;
        final double newMinY = minY - deltaY;
        final double newMaxY = maxY + deltaY;
        if (newMinX > newMaxX || newMinY > newMaxY) {
            // This yields a null envelope
            return new Envelope();
        }
        return new Envelope(newMinX, newMaxX, newMinY, newMaxY);
    }

    /**
     * Returns a new Envelope expanded to include (x, y).
     */
    public Envelope expandToInclude(
        final double x,
        final double y
    ) {
        if (isNull()) {
            return new Envelope(x, x, y, y);
        }
        final double newMinX = Math.min(minX, x);
        final double newMaxX = Math.max(maxX, x);
        final double newMinY = Math.min(minY, y);
        final double newMaxY = Math.max(maxY, y);
        return new Envelope(newMinX, newMaxX, newMinY, newMaxY);
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
        if (other.isNull()) {
            return this;
        }
        if (this.isNull()) {
            return new Envelope(other);
        }
        final double newMinX = Math.min(this.minX, other.minX);
        final double newMaxX = Math.max(this.maxX, other.maxX);
        final double newMinY = Math.min(this.minY, other.minY);
        final double newMaxY = Math.max(this.maxY, other.maxY);
        return new Envelope(newMinX, newMaxX, newMinY, newMaxY);
    }

    /**
     * Returns a new Envelope translated by transX, transY.
     */
    public Envelope translate(
        final double transX,
        final double transY
    ) {
        if (isNull()) {
            return this;
        }
        return new Envelope(
            minX + transX, maxX + transX,
            minY + transY, maxY + transY
        );
    }

    public Coordinate centre() {
        if (isNull()) {
            return null;
        }
        return new Coordinate((minX + maxX) / 2.0, (minY + maxY) / 2.0);
    }

    public Envelope intersection(final Envelope env) {
        if (this.isNull() || env.isNull() || !this.intersects(env)) {
            return new Envelope();
        }
        final double intMinX = Math.max(this.minX, env.minX);
        final double intMaxX = Math.min(this.maxX, env.maxX);
        final double intMinY = Math.max(this.minY, env.minY);
        final double intMaxY = Math.min(this.maxY, env.maxY);
        return new Envelope(intMinX, intMaxX, intMinY, intMaxY);
    }

    public boolean intersects(final Envelope other) {
        if (this.isNull() || other.isNull()) return false;
        if (other.minX > this.maxX) return false;
        if (other.maxX < this.minX) return false;
        if (other.minY > this.maxY) return false;
        return !(other.maxY < this.minY);
    }

    @Deprecated
    public boolean overlaps(final Envelope other) {
        return intersects(other);
    }

    public boolean intersects(final Coordinate p) {
        return intersects(p.lon, p.lat);
    }

    @Deprecated
    public boolean overlaps(final Coordinate p) {
        return intersects(p);
    }

    public boolean intersects(
        final double x,
        final double y
    ) {
        if (isNull()) return false;
        if (x > maxX) return false;
        if (x < minX) return false;
        if (y > maxY) return false;
        return !(y < minY);
    }

    @Deprecated
    public boolean overlaps(
        final double x,
        final double y
    ) {
        return intersects(x, y);
    }

    public boolean contains(final Envelope other) {
        return covers(other);
    }

    public boolean contains(final Coordinate p) {
        return covers(p);
    }

    public boolean contains(
        final double x,
        final double y
    ) {
        return covers(x, y);
    }

    public boolean covers(
        final double x,
        final double y
    ) {
        if (isNull()) return false;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    // -----------------------------------------------------------------------
    // Static JTS methods, copied as-is
    // -----------------------------------------------------------------------

    public boolean covers(final Coordinate p) {
        return covers(p.lon, p.lat);
    }

    public boolean covers(final Envelope other) {
        if (this.isNull() || other.isNull()) return false;
        return other.minX >= this.minX && other.maxX <= this.maxX
            && other.minY >= this.minY && other.maxY <= this.maxY;
    }

    // -----------------------------------------------------------------------
    // Conversion to/from JTS
    // -----------------------------------------------------------------------

    public double distance(final Envelope env) {
        if (this.intersects(env)) {
            return 0.0;
        }
        double dx = 0.0;
        if (this.maxX < env.minX) {
            dx = env.minX - this.maxX;
        } else if (this.minX > env.maxX) {
            dx = this.minX - env.maxX;
        }
        double dy = 0.0;
        if (this.maxY < env.minY) {
            dy = env.minY - this.maxY;
        } else if (this.minY > env.maxY) {
            dy = this.minY - env.maxY;
        }
        if (dx == 0.0) return dy;
        if (dy == 0.0) return dx;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Converts this immutable Envelope to a JTS Envelope.
     */
    public com.vividsolutions.jts.geom.Envelope toJTS() {
        return new com.vividsolutions.jts.geom.Envelope(minX, maxX, minY, maxY);
    }
}
