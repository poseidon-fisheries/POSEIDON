/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import java.util.Map.Entry;
import java.util.function.ToDoubleFunction;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.poseidon.common.core.Entry.entry;

public class EvaluatorBasedFadDeactivationStrategy extends FadDeactivationStrategy {
    private static final long serialVersionUID = -2818076865902570581L;
    private final ToDoubleFunction<? super Fad> fadEvaluator;

    @SuppressWarnings("WeakerAccess")
    public EvaluatorBasedFadDeactivationStrategy(final ToDoubleFunction<? super Fad> fadEvaluator) {
        this.fadEvaluator = fadEvaluator;
    }

    @Override
    protected void deactivate(final int numberOfFadsToDeactivate) {
        getFadManager()
            .getDeployedFads()
            .stream()
            // it's important to evaluate the FADs before sorted because the value might be noisy and
            // then something like `comparingDouble(fadEvaluator)` wouldn't be a stable comparison
            .map(fad -> entry(fad, fadEvaluator.applyAsDouble(fad)))
            .sorted(comparingDouble(Entry::getValue))
            .map(Entry::getKey)
            .limit(numberOfFadsToDeactivate)
            .collect(toList())
            .forEach(fad -> getFadManager().loseFad(fad));
    }
}
