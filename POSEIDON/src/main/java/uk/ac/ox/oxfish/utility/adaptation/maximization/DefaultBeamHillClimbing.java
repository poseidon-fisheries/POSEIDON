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
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.function.Predicate;

/**
 * Pre-made hillclimber specifically for moving around maps
 * Created by carrknight on 8/7/15.
 */
public class DefaultBeamHillClimbing extends BeamHillClimbing<SeaTile> {


    public DefaultBeamHillClimbing(int maxStep, int attempts) {
        this(DEFAULT_ALWAYS_COPY_BEST, DEFAULT_DYNAMIC_NETWORK, maxStep, attempts, true);
    }

    public DefaultBeamHillClimbing(
        boolean copyAlwaysBest, Predicate<Pair<Double, Double>> unfriendPredicate,
        int maxStep, int attempts, final boolean backtracksOnBadExploration
    ) {
        super(copyAlwaysBest, backtracksOnBadExploration, unfriendPredicate,
            DEFAULT_RANDOM_STEP(maxStep, attempts)
        );
    }

    final public static RandomStep<SeaTile> DEFAULT_RANDOM_STEP(int maxStep, int attempts) {
        return new RandomStep<SeaTile>() {
            @Override
            public SeaTile randomStep(
                FishState state, MersenneTwisterFast random, Fisher fisher, SeaTile current
            ) {
                for (int i = 0; i < attempts; i++) {
                    int x = current.getGridX() + (random.nextBoolean() ? random.nextInt(maxStep + 1) : -random.nextInt(
                        maxStep + 1));
                    int y = current.getGridY() + (random.nextBoolean() ? random.nextInt(maxStep + 1) : -random.nextInt(
                        maxStep + 1));
                    SeaTile candidate = state.getMap().getSeaTile(x, y);
                    if (candidate != null && current != candidate && candidate.isWater()
                        && !fisher.getHomePort().getLocation().equals(candidate))
                        return candidate;
                }

                //stay where you are
                return current;
            }
        };
    }

    static public DefaultBeamHillClimbing BeamHillClimbingWithUnfriending(
        boolean alwaysCopyBest,
        final double unfriendingThreshold,
        int maxSteps, int attempts
    ) {
        Preconditions.checkArgument(unfriendingThreshold >= 0, "Unfriending threshold should be above 0!");
        return new DefaultBeamHillClimbing(alwaysCopyBest,
            oldFitnessAndNew ->
                unfriendingThreshold * oldFitnessAndNew.getFirst() >
                    oldFitnessAndNew.getSecond(),
            maxSteps,
            attempts, true
        );
    }


}
