/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;

public class AbundanceCatchMakerTest {

    @Test
    public void testApply() {

        final Species twoBinner = new Species(
            "TwoBinner",
            new GrowthBinByList(2, new double[2], new double[2])
        );
        final Species threeBinner = new Species(
            "ThreeBinner",
            new GrowthBinByList(2, new double[3], new double[3])
        );
        final GlobalBiology globalBiology = new GlobalBiology(twoBinner, threeBinner);
        final CatchMaker<AbundanceLocalBiology> catchMaker = new AbundanceCatchMaker(globalBiology);

        final AbundanceLocalBiology desiredBiology =
            new AbundanceLocalBiology(ImmutableMap.of(
                twoBinner, new double[][]{
                    new double[]{100, 101},
                    new double[]{110, 111}
                },
                threeBinner, new double[][]{
                    new double[]{200, 201, 202},
                    new double[]{210, 211, 212}
                }
            ));

        final AbundanceLocalBiology emptyAvailableBiology =
            new AbundanceLocalBiology(globalBiology);

        final Entry<Catch, AbundanceLocalBiology> emptyCatch =
            catchMaker.apply(emptyAvailableBiology, desiredBiology);

        globalBiology.getSpecies().forEach(species -> {
            Assertions.assertArrayEquals(emptyAvailableBiology.getAbundance(species).asMatrix(),
                requireNonNull(emptyCatch.getKey().getAbundance(species)).asMatrix());
            Assertions.assertArrayEquals(desiredBiology.getAbundance(species).asMatrix(),
                emptyCatch.getValue().getAbundance(species).asMatrix());
        });

        final AbundanceLocalBiology availableBiology =
            new AbundanceLocalBiology(ImmutableMap.of(
                twoBinner, new double[][]{
                    new double[]{100, 100},
                    new double[]{100, 100}
                },
                threeBinner, new double[][]{
                    new double[]{100, 100, 100},
                    new double[]{100, 100, 100}
                }
            ));

        final Entry<Catch, AbundanceLocalBiology> betterCatch =
            catchMaker.apply(availableBiology, desiredBiology);

        globalBiology.getSpecies().forEach(species -> {
            Assertions.assertArrayEquals(availableBiology.getAbundance(species).asMatrix(),
                requireNonNull(betterCatch.getKey().getAbundance(species)).asMatrix());
            Assertions.assertArrayEquals(new AbundanceLocalBiology(ImmutableMap.of(
                twoBinner, new double[][]{
                    new double[]{0, 1},
                    new double[]{10, 11}
                },
                threeBinner, new double[][]{
                    new double[]{100, 101, 102},
                    new double[]{110, 111, 112}
                }
            )).getAbundance(species).asMatrix(), betterCatch.getValue().getAbundance(species).asMatrix());
        });

    }
}