/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility;

/**
 * Created by carrknight on 10/6/16.
 *
 * @deprecated Use {@link java.util.AbstractMap.SimpleImmutableEntry} for immutable pairs
 * (easily created with {@link FishStateUtilities#entry(Object, Object) entry}), or a
 * two-element array if you really need mutability.
 */
@Deprecated
public class MutablePair<A, B> {


    private A first;

    private B second;

    public MutablePair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }

    /**
     * Setter for property 'first'.
     *
     * @param first Value to set for property 'first'.
     */
    public void setFirst(final A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    /**
     * Setter for property 'second'.
     *
     * @param second Value to set for property 'second'.
     */
    public void setSecond(final B second) {
        this.second = second;
    }
}
