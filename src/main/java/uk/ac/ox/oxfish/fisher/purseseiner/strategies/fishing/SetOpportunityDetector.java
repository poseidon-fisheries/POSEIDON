/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.min;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;

public class SetOpportunityDetector<B extends LocalBiology> {

    private final Map<SetOpportunityGenerator<B, ? extends AbstractSetAction<B>>, Double>
        detectionProbabilities;
    private final double searchBonus;

    private final Fisher fisher;
    private final MersenneTwisterFast rng;

    private boolean hasSearched = false;

    public SetOpportunityDetector(
        final Fisher fisher,
        final Map<
            SetOpportunityGenerator<B, ? extends AbstractSetAction<B>>,
            Double> detectionProbabilities,
        final double searchBonus
    ) {
        checkArgument(
            detectionProbabilities.values().stream().allMatch(v -> v >= 0 && v <= 1)
        );
        checkArgument(searchBonus >= 0 && searchBonus <= 1);
        this.fisher = fisher;
        this.rng = fisher.grabRandomizer();
        this.detectionProbabilities = ImmutableMap.copyOf(detectionProbabilities);
        this.searchBonus = searchBonus;
    }

    @NotNull
    List<AbstractSetAction<B>> possibleSetActions() {
        final double bonus = hasSearched ? searchBonus : 0;
        hasSearched = false;
        return fisher.getHold().getPercentageFilled() >= 1
            ? ImmutableList.of() // no possible sets when hold is full
            : detectionProbabilities
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    final double p = min(1.0, entry.getValue() + bonus);
                    return entry.getKey().apply(fisher).stream()
                        .filter(__ -> rng.nextBoolean(p));
                })
                .collect(toImmutableList());
    }

    public void notifyOfSearch() {
        hasSearched = true;
    }

}
