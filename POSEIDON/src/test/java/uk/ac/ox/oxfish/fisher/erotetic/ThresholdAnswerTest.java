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

package uk.ac.ox.oxfish.fisher.erotetic;

import org.jfree.util.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class ThresholdAnswerTest {


    private LinkedList<Double> options;
    private FeatureExtractors<Double> extractor;

    @BeforeEach
    public void setUp() throws Exception {
        options = new LinkedList<>();
        options.add(1d);
        options.add(2d);
        options.add(3d);
        extractor = new FeatureExtractors<>();
        extractor.addFeatureExtractor("feature", (toRepresent, model, fisher) -> {
            HashMap<Double, Double> toReturn = new HashMap<>();
            for (Double number : toRepresent)
                toReturn.put(number, number);
            return toReturn;
        });
    }

    @Test
    public void tooLow() throws Exception {


        Log.info("needs numbers to be above 10, will not select any");
        ThresholdAnswer<Double> filter = new ThresholdAnswer<>(
            2,
            10,
            "feature"
        );


        List<Double> selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );

        assertTrue(selected == null || selected.isEmpty());

    }


    @Test
    public void tooFew() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdAnswer<Double> filter = new ThresholdAnswer<>(
            4,
            0,
            "feature"
        );


        List<Double> selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );

        assertTrue(selected == null || selected.isEmpty());

    }


    @Test
    public void allGood() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdAnswer<Double> filter = new ThresholdAnswer<>(
            3,
            0,
            "feature"
        );


        List<Double> selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)

        );

        assertEquals(selected.size(), 3);

    }

    @Test
    public void oneGood() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        ThresholdAnswer<Double> filter = new ThresholdAnswer<>(
            3,
            3,
            "feature"
        );


        List<Double> selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );

        assertEquals(selected.size(), 1);

    }
}