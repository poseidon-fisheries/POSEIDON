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

package uk.ac.ox.oxfish.utility.adaptation.maximization;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Pre-made hillclimber specifically for moving around maps
 * Created by carrknight on 8/7/15.
 */
public class DefaultBeamHillClimbing extends BeamHillClimbing<SeaTile> {


    public DefaultBeamHillClimbing(final int maxStep, final int attempts) {
        this(DEFAULT_ALWAYS_COPY_BEST, DEFAULT_DYNAMIC_NETWORK, maxStep, attempts, true);
    }

    public DefaultBeamHillClimbing(
        final boolean copyAlwaysBest, final Predicate<Map.Entry<Double, Double>> unfriendPredicate,
        final int maxStep, final int attempts, final boolean backtracksOnBadExploration
    ) {
        super(copyAlwaysBest, backtracksOnBadExploration, unfriendPredicate,
            DEFAULT_RANDOM_STEP(maxStep, attempts)
        );
    }

    final public static RandomStep<SeaTile> DEFAULT_RANDOM_STEP(final int maxStep, final int attempts) {
        return (state, random, fisher, current) -> {
            for (int i = 0; i < attempts; i++) {
                final int x = current.getGridX() + (random.nextBoolean() ? random.nextInt(maxStep + 1) : -random.nextInt(
                    maxStep + 1));
                final int y = current.getGridY() + (random.nextBoolean() ? random.nextInt(maxStep + 1) : -random.nextInt(
                    maxStep + 1));
                final SeaTile candidate = state.getMap().getSeaTile(x, y);
                if (candidate != null && current != candidate && candidate.isWater()
                    && !fisher.getHomePort().getLocation().equals(candidate))
                    return candidate;
            }

            //stay where you are
            return current;
        };
    }

    static public DefaultBeamHillClimbing BeamHillClimbingWithUnfriending(
        final boolean alwaysCopyBest,
        final double unfriendingThreshold,
        final int maxSteps, final int attempts
    ) {
        Preconditions.checkArgument(unfriendingThreshold >= 0, "Unfriending threshold should be above 0!");
        return new DefaultBeamHillClimbing(alwaysCopyBest,
            oldFitnessAndNew ->
                unfriendingThreshold * oldFitnessAndNew.getKey() >
                    oldFitnessAndNew.getValue(),
            maxSteps,
            attempts, true
        );
    }


}
