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

package uk.ac.ox.oxfish.biology;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;


public class MeristicsTest {


    @Test
    public void yelloweye() throws Exception {


        final StockAssessmentCaliforniaMeristics yellowEye = new StockAssessmentCaliforniaMeristics(
            100,
            70,
            1,
            18.717,
            64.594,
            0.047,
            0.000017,
            3.03,
            0.045,
            1,
            18.717,
            62.265,
            0.047,
            0.00000977,
            3.17,
            0.046,
            38.78,
            -0.437,
            137900,
            36500,
            228149,
            0.44056,
            true
        );

        //see if age 5 was computed correctly
        assertEquals(yellowEye.getLength(FishStateUtilities.FEMALE, 5), 26.4837518217, .001);
        assertEquals(yellowEye.getLength(FishStateUtilities.MALE, 5), 26.8991271545, .001);

        assertEquals(yellowEye.getWeight(FishStateUtilities.FEMALE, 5), 0.3167667645, .001);
        assertEquals(yellowEye.getWeight(FishStateUtilities.MALE, 5), 0.365220907, .001);

        assertEquals(yellowEye.getMaturity()[5], 0.0046166415, .0001);
        assertEquals(yellowEye.getRelativeFecundity()[5], 47344.590014727, .001);
        assertEquals(yellowEye.getPhi().get(5), 173.6635925757, .001);

        assertEquals(yellowEye.getCumulativeSurvivalFemale().get(5), 0.7945336025, .001);
        assertEquals(yellowEye.getCumulativePhi(), 8043057.98636817, .01);

    }


    @Test
    public void shortspine() throws Exception {


        final StockAssessmentCaliforniaMeristics shortspine = new StockAssessmentCaliforniaMeristics(
            100,
            100,
            2,
            7,
            75,
            0.018,
            4.77E-06,
            3.263,
            0.0505,
            2,
            7,
            75,
            0.018,
            4.77E-06,
            3.263,
            0.0505,
            18.2,
            -2.3,
            1,
            0,
            36315502,
            0.6,
            false
        );

        //see if age 5 was computed correctly
        assertEquals(shortspine.getLength(FishStateUtilities.FEMALE, 5), 11.3138255265, .001);
        assertEquals(shortspine.getLength(FishStateUtilities.MALE, 5), 11.3138255265, .001);

        assertEquals(shortspine.getWeight(FishStateUtilities.FEMALE, 5), 0.0130770514, .001);
        assertEquals(shortspine.getWeight(FishStateUtilities.MALE, 5), 0.0130770514, .001);

        assertEquals(shortspine.getMaturity()[10], 0.3900004207, .0001);
        assertEquals(shortspine.getRelativeFecundity()[5], 0.0130770514, .001);
        assertEquals(shortspine.getRelativeFecundity()[20], 0.3052767163, .001);
        assertEquals(shortspine.getCumulativeSurvivalFemale().get(5), 0.7768562128, .001);
        assertEquals(shortspine.getCumulativeSurvivalFemale().get(20), 0.3642189796, .001);
        assertEquals(shortspine.getPhi().get(20), 0.1111875741, .001);
        assertEquals(shortspine.getCumulativePhi(), 10.9714561805, .01);

    }

    @Test
    public void longspine() throws Exception {


        final StockAssessmentCaliforniaMeristics longspine = new StockAssessmentCaliforniaMeristics(
            80,
            40,
            3,
            8.573,
            27.8282,
            0.108505,
            4.30E-06,
            3.352,
            0.111313,
            3,
            8.573,
            27.8282,
            0.108505,
            4.30E-06,
            3.352,
            0.111313,
            17.826,
            -1.79,
            1,
            0,
            168434124,
            0.6,
            false
        );

        //see if age 5 was computed correctly
        assertEquals(longspine.getLength(FishStateUtilities.FEMALE, 5), 12.3983090675, .001);
        assertEquals(longspine.getLength(FishStateUtilities.MALE, 5), 12.3983090675, .001);

        assertEquals(longspine.getWeight(FishStateUtilities.FEMALE, 5), 0.019880139, .001);
        assertEquals(longspine.getWeight(FishStateUtilities.MALE, 5), 0.019880139, .001);

        assertEquals(longspine.getMaturity()[5], 6.03332555676691E-05, .0001);
        assertEquals(longspine.getRelativeFecundity()[5], 0.019880139, .001);
        assertEquals(longspine.getCumulativeSurvivalMale().get(5), 0.5731745408, .001);
        assertEquals(longspine.getPhi().get(5), 3.45814860523815E-05, .001);

        assertEquals(longspine.getCumulativePhi(), 0.5547290727, .001);

    }

    @Test
    public void sablefish() throws Exception {


        final StockAssessmentCaliforniaMeristics sablefish = new StockAssessmentCaliforniaMeristics(
            59,
            30,
            0.5,
            25.8,
            56.2,
            0.419,
            3.6724E-06,
            3.250904,
            0.065,
            0.5,
            25.8,
            64,
            0.335,
            3.4487E-06,
            3.26681,
            0.08,
            58,
            -0.13,
            1,
            0,
            40741397,
            0.6,
            false
        );

        //see if age 5 was computed correctly
        assertEquals(sablefish.getLength(FishStateUtilities.FEMALE, 5), 55.5416341677, .001);
        assertEquals(sablefish.getLength(FishStateUtilities.MALE, 5), 51.5868143025, .001);

        assertEquals(sablefish.getWeight(FishStateUtilities.FEMALE, 5), 1.7258103959, .001);
        assertEquals(sablefish.getWeight(FishStateUtilities.MALE, 5), 1.3559663707, .001);

        assertEquals(sablefish.getMaturity()[5], 0.4207762664, .0001);
        assertEquals(sablefish.getRelativeFecundity()[5], 1.7258103959, .001);
        assertEquals(sablefish.getCumulativeSurvivalFemale().get(5), 0.670320046, .001);
        assertEquals(sablefish.getPhi().get(5), 0.4867730478, .001);
        assertEquals(sablefish.getCumulativePhi(), 14.2444066772, .001);

    }


    @Test
    public void doverSole() throws Exception {


        final StockAssessmentCaliforniaMeristics sole = new StockAssessmentCaliforniaMeristics(
            69,
            50,
            1,
            9.04,
            39.91,
            0.1713,
            0.000002231,
            3.412,
            0.1417,
            1,
            5.4,
            47.81,
            0.1496,
            0.000002805,
            3.345,
            0.1165,
            35,
            -0.775,
            1,
            0,
            404138330,
            0.8,
            false
        );

        //see if age 5 was computed correctly
        assertEquals(sole.getLength(FishStateUtilities.FEMALE, 5), 24.5101516433, .001);
        assertEquals(sole.getLength(FishStateUtilities.MALE, 5), 24.3553122333, .001);

        assertEquals(sole.getWeight(FishStateUtilities.FEMALE, 5), 0.124536091, .001);
        assertEquals(sole.getWeight(FishStateUtilities.MALE, 5), 0.120103947, .001);

        assertEquals(sole.getMaturity()[5], 0.0002945897, .0001);

        assertEquals(sole.getRelativeFecundity()[5], 0.124536091, .001);
        assertEquals(sole.getCumulativeSurvivalFemale().get(5), 0.5585003689, .001);
        assertEquals(sole.getPhi().get(5), 2.04897288861722E-05, .001);

        assertEquals(sole.getCumulativePhi(), 2.3584385374, .001);

    }


    @Test
    public void canaryRockfish() throws Exception {


        final StockAssessmentCaliforniaMeristics canary = new StockAssessmentCaliforniaMeristics(
            40,
            20,
            1,
            8.04,
            52.53,
            0.16,
            1.55E-05,
            3.03,
            0.06,
            1,
            8.04,
            60.36,
            0.125,
            1.55E-05,
            3.03,
            0.06,
            40.5,
            -0.25,
            1,
            0,
            38340612,
            0.511,
            true
        );

        //see if age 5 was computed correctly
        assertEquals(canary.getLength(FishStateUtilities.FEMALE, 5), 30.7375135093, .001);
        assertEquals(canary.getWeight(FishStateUtilities.FEMALE, 5), 0.4988476814, .001);

        //todo ask to steve for discrepancy
        assertEquals(canary.getLength(FishStateUtilities.MALE, 5), 30.1273037904, .001);
        assertEquals(canary.getWeight(FishStateUtilities.MALE, 5), 0.4694413124, .001);

        assertEquals(canary.getMaturity()[5], 0.0801270824, .0001);
        assertEquals(canary.getRelativeFecundity()[5], 0.4988476814, .001);
        assertEquals(canary.getCumulativeSurvivalFemale().get(5), 0.7408182207, .001);

        assertEquals(canary.getPhi().get(5), 0.0296114001, .001);

        assertEquals(canary.getCumulativePhi(), 29.024080491, .001);
    }


    @Test
    public void placeholder() throws Exception {
        final Meristics placeholder = StockAssessmentCaliforniaMeristics.FAKE_MERISTICS;
        assertEquals(placeholder.getWeight(FishStateUtilities.MALE, 0), 1, .001);
        assertEquals(placeholder.getWeight(FishStateUtilities.FEMALE, 0), 1, .001);
        assertEquals(placeholder.getNumberOfBins(), 1, .001);


    }
}