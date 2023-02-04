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

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The simplest pair object
 * Created by carrknight on 5/4/15.
 */
public class Pair<A,B> {

    final private A first;

    final private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    /** Create a new pair from the first element of the pair and the result of a function applied to the second element */
    public <R> Pair<A, R> mapSecond(Function<B, R> f) { return new Pair<>(first, f.apply(second)); }

    /** Create a new pair from the first element of the pair and the result of a function applied to both elements */
    public <R> Pair<A, R> mapSecond(BiFunction<A, B, R> f) { return new Pair<>(first, f.apply(first, second)); }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Pair{");
        sb.append("first=").append(first);
        sb.append(", second=").append(second);
        sb.append('}');
        return sb.toString();
    }
}
