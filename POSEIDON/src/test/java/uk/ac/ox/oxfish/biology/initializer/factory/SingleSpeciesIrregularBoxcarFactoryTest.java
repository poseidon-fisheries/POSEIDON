/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.boxcars.BoxCarSimulator;
import uk.ac.ox.oxfish.biology.boxcars.FixedBoxcarAging;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RepeatingInitialAbundance;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SingleSpeciesIrregularBoxcarFactoryTest {


    @Test
    public void irregularBoxcar() {
        final FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());


        //check that the length and weights match the regular ones
        final SingleSpeciesRegularBoxcarFactory control = new SingleSpeciesRegularBoxcarFactory();
        control.setCmPerBin(5);
        final SingleSpeciesAbundanceInitializer equalSpaced = control.apply(mock);

        final SingleSpeciesIrregularBoxcarFactory factory = new SingleSpeciesIrregularBoxcarFactory();

        factory.setBinnedLengthsInCm(Lists.newArrayList(12.5d, 57.5d, 102.5d));
        final SingleSpeciesAbundanceInitializer unequalSpaced = factory.apply(mock);

        //same lengths/same weights
        //12.5 cm
        Assertions.assertEquals(
            equalSpaced.getMeristics().getLength(0, 2),
            unequalSpaced.getMeristics().getLength(0, 0),
            .0001
        );
        Assertions.assertEquals(
            equalSpaced.getMeristics().getWeight(0, 2),
            unequalSpaced.getMeristics().getWeight(0, 0),
            .0001
        );
        //57.5
        Assertions.assertEquals(
            equalSpaced.getMeristics().getLength(0, 11),
            unequalSpaced.getMeristics().getLength(0, 1),
            .0001
        );
        Assertions.assertEquals(
            equalSpaced.getMeristics().getWeight(0, 11),
            unequalSpaced.getMeristics().getWeight(0, 1),
            .0001
        );

        //102.5
        Assertions.assertEquals(
            equalSpaced.getMeristics().getLength(0, 20),
            unequalSpaced.getMeristics().getLength(0, 2),
            .0001
        );
        Assertions.assertEquals(
            equalSpaced.getMeristics().getWeight(0, 20),
            unequalSpaced.getMeristics().getWeight(0, 2),
            .0001
        );
    }


    @Test
    public void noNoiseRecruitmentUsedForCarryingCapacityEvenWhenRecruitmentIsNoisy() {
        final FishState mock = mock(FishState.class);
        when(mock.getRandom()).thenReturn(new MersenneTwisterFast());


        final SingleSpeciesIrregularBoxcarFactory factory = new SingleSpeciesIrregularBoxcarFactory();

        factory.setBinnedLengthsInCm(Lists.newArrayList(12.5d, 57.5d, 102.5d));
        factory.setInitialBtOverK(new FixedDoubleParameter(1d));
        final SingleSpeciesAbundanceInitializer noNoise = factory.apply(mock);

        factory.setRecruitmentNoiseStartingYear(new FixedDoubleParameter(0));
        factory.setRecruitmentProcessStandardDeviation(new FixedDoubleParameter(0.4));
        final SingleSpeciesAbundanceInitializer noise = factory.apply(mock);

        //the initial abundance ought to be the same (since that's carrying capacity!)
        Assertions.assertArrayEquals(
            ((RepeatingInitialAbundance) noNoise.getInitialAbundance()).peekCohort(),
            ((RepeatingInitialAbundance) noise.getInitialAbundance()).peekCohort(),
            1
        );

    }

    @Test
    public void recruitmentNoiseWillChangeSimulationResults() {
        final FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());


        final SingleSpeciesIrregularBoxcarFactory factory = new SingleSpeciesIrregularBoxcarFactory();

        factory.setBinnedLengthsInCm(Lists.newArrayList(12.5d, 57.5d, 102.5d));
        factory.setInitialBtOverK(new FixedDoubleParameter(.5d));
        final SingleSpeciesAbundanceInitializer noNoise = factory.apply(state);

        final GrowthBinByList meristicsInstance = (GrowthBinByList) noNoise.getMeristics();
        BoxCarSimulator simulator = new BoxCarSimulator(
            ((RecruitmentBySpawningBiomass) noNoise.getRecruitmentProcess()).getVirginRecruits(),
            ((FixedBoxcarAging) noNoise.getAging()),
            ((RecruitmentBySpawningBiomass) noNoise.getRecruitmentProcess()),
            meristicsInstance,
            noNoise.getMortality()
        );
        final double totalWeightNoNoise = FishStateUtilities.weigh(
            simulator.virginCondition(state, 20),
            meristicsInstance
        );
        System.out.println(totalWeightNoNoise);

        //all no noise runs will generate exactly the same simulated numbers
        for (int attempts = 0; attempts < 10; attempts++) {
            final SingleSpeciesAbundanceInitializer initializer = factory.apply(state);
            simulator = new BoxCarSimulator(
                ((RecruitmentBySpawningBiomass) initializer.getRecruitmentProcess()).getVirginRecruits(),
                ((FixedBoxcarAging) initializer.getAging()),
                ((RecruitmentBySpawningBiomass) initializer.getRecruitmentProcess()),
                meristicsInstance,
                initializer.getMortality()
            );
            final double newWeight = FishStateUtilities.weigh(
                simulator.virginCondition(state, 20),
                meristicsInstance
            );
            Assertions.assertEquals(totalWeightNoNoise, newWeight, .0001);
        }

        //but if it is with noise, completely different numbers will get simulated... that's noise for you.
        //all no noise runs will generate exactly the same simulated numbers
        factory.setRecruitmentNoiseStartingYear(new FixedDoubleParameter(-1));
        factory.setRecruitmentProcessStandardDeviation(new FixedDoubleParameter(.3));
        for (int attempts = 0; attempts < 10; attempts++) {
            final SingleSpeciesAbundanceInitializer initializer = factory.apply(state);
            simulator = new BoxCarSimulator(
                ((RecruitmentBySpawningBiomass) initializer.getRecruitmentProcess()).getVirginRecruits(),
                ((FixedBoxcarAging) initializer.getAging()),
                ((RecruitmentBySpawningBiomass) initializer.getRecruitmentProcess()),
                meristicsInstance,
                initializer.getMortality()
            );
            final double newWeight = FishStateUtilities.weigh(
                simulator.virginCondition(state, 20),
                meristicsInstance
            );
            assertNotEquals(totalWeightNoNoise, newWeight, 1000);
        }

    }

}
