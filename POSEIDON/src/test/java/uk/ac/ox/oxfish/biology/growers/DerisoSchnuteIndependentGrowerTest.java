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

package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 2/1/17.
 */
public class DerisoSchnuteIndependentGrowerTest {


    @Test
    public void stayVirgin() throws Exception {

        //numbers are from sablefish

        BiomassLocalBiology biology =
            new BiomassLocalBiology(
                new double[]{527154d},
                new double[]{527154d}
            );

        ArrayList<Double> biomasses = Lists.newArrayList(527154d, 527154d, 527154d, 527154d, 527154d, 527154d);
        DerisoSchnuteIndependentGrower grower =
            new DerisoSchnuteIndependentGrower(
                biomasses, 1.03, 0.92311,
                0.6, 3, 0,
                1.03313,
                1.01604,
                527154d,
                29728.8

            );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }
        Assert.assertEquals(biology.getCurrentBiomass()[0], 527154d, .001);

    }

    @Test
    public void fivePercentFishingFromVirginTest() throws Exception {

        //numbers are from sablefish

        BiomassLocalBiology biology =
            new BiomassLocalBiology(
                new double[]{527154d},
                new double[]{527154d}
            );

        ArrayList<Double> biomasses = Lists.newArrayList(527154d, 527154d, 527154d, 527154d, 527154d, 527154d);
        DerisoSchnuteIndependentGrower grower =
            new DerisoSchnuteIndependentGrower(
                biomasses, 1.03, 0.92311,
                0.6, 3, 0,
                1.03313,
                1.01604,
                527154d,
                29728.8

            );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            biology.getCurrentBiomass()[0] = biology.getCurrentBiomass()[0] * .95; //5% mortality!
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

        Assert.assertEquals(297073.2, biology.getCurrentBiomass()[0], .0001);
    }

    @Test
    public void fivePercentFishingFromVirginTestYelloweye() throws Exception {

        //numbers are from sablefish

        double virginB = 8883d;
        double virginV = 85.13962;
        BiomassLocalBiology biology =
            new BiomassLocalBiology(
                new double[]{virginB},
                new double[]{virginB}
            );

        ArrayList<Double> biomasses = Lists.newArrayList(virginB, virginB, virginB, virginB, virginB,
            virginB, virginB, virginB, virginB, virginB,
            virginB, virginB, virginB, virginB, virginB,
            virginB, virginB, virginB, virginB, virginB,
            virginB, virginB
        );

        //notice that these aren't the correct yelloweye numbers (rho is from sablefish)
        //but these were the numbers in the R script and the factual accuracy of the
        //biology is not important here, the numbers have to be the same though

        DerisoSchnuteIndependentGrower grower =
            new DerisoSchnuteIndependentGrower(
                biomasses, 0.922, 0.95504,
                0.44056, 14, 0,
                1.11910,
                .63456,
                virginB,
                virginV

            );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 20; i++) {
            biology.getCurrentBiomass()[0] = biology.getCurrentBiomass()[0] * .95; //5% mortality!
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

        Assert.assertEquals(4184.71, biology.getCurrentBiomass()[0], .0001);
    }

    /*
    @Test
    public void noFishingTest() throws Exception {

        //numbers are from sablefish

        BiomassLocalBiology biology =
                new BiomassLocalBiology(new Double[]{205662d},
                                         new Double[]{527154d});

        ArrayList<Double> data = Lists.newArrayList(263494d, 248481d, 233025d, 222936d, 211793d, 205662d);
        DerisoSchnuteIndependentGrower grower =
                new DerisoSchnuteIndependentGrower(
                        data, 0.92267402483245d, 0.923116346386636,
                        0.6, 3, 0,
                        1.03312585773941,
                        0.634560212266768
                );

        grower.getBiologies().add(biology);

        grower.start(mock(FishState.class));

        for (int i = 0; i < 100; i++) {
            grower.step(mock(FishState.class));
            System.out.println(biology.getCurrentBiomass()[0]);
        }

    }
    */
}