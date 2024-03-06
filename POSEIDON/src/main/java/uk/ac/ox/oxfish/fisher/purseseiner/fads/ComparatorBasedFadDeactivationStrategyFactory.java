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

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;

import java.util.Comparator;

public class ComparatorBasedFadDeactivationStrategyFactory implements ComponentFactory<FadDeactivationStrategy> {

    private ComponentFactory<Comparator<Fad>> fadComparator;

    @SuppressWarnings("unused")
    public ComparatorBasedFadDeactivationStrategyFactory() {
    }

    public ComparatorBasedFadDeactivationStrategyFactory(final ComponentFactory<Comparator<Fad>> fadComparator) {
        this.fadComparator = fadComparator;
    }

    @SuppressWarnings("unused")
    public ComponentFactory<Comparator<Fad>> getFadComparator() {
        return fadComparator;
    }

    @SuppressWarnings("unused")
    public void setFadComparator(final ComponentFactory<Comparator<Fad>> fadComparator) {
        this.fadComparator = fadComparator;
    }

    @Override
    public FadDeactivationStrategy apply(final ModelState modelState) {
        return new ComparatorBasedFadDeactivationStrategy(fadComparator.apply(modelState));
    }
}
