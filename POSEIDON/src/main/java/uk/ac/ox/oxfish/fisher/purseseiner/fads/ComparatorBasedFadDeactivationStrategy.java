/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import java.util.Comparator;

import static java.util.stream.Collectors.toList;

public class ComparatorBasedFadDeactivationStrategy extends FadDeactivationStrategy {
    private final Comparator<Fad> fadComparator;

    public ComparatorBasedFadDeactivationStrategy(final Comparator<Fad> fadComparator) {
        this.fadComparator = fadComparator;
    }

    @Override
    protected void deactivate(final int numberOfFadsToDeactivate) {
        getFadManager()
            .getDeployedFads()
            .stream()
            .sorted(fadComparator)
            .limit(numberOfFadsToDeactivate)
            .collect(toList())
            .forEach(fad -> getFadManager().loseFad(fad));
    }
}
