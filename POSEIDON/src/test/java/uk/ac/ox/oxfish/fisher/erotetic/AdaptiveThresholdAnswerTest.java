/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.erotetic;

import org.jfree.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/2/16.
 */
public class AdaptiveThresholdAnswerTest {

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
            final HashMap<Double, Double> toReturn = new HashMap<>();
            for (final Double number : toRepresent)
                toReturn.put(number, number);
            return toReturn;
        });
    }


    @SuppressWarnings("unchecked")
    @Test
    public void adapt() throws Exception {


        Log.info("needs at least 4 observations, has only 3");
        final FeatureExtractor<Double> adaptor = mock(FeatureExtractor.class);
        extractor.addFeatureExtractor(
            "threshold",
            adaptor
        );

        final HashMap<Double, Double> adaptorAnswer = mock(HashMap.class);
        when(adaptor.extractFeature(anyCollection(),
            any(), any()
        )).thenReturn(adaptorAnswer);

        final FeatureThresholdAnswer<Double> filter = new FeatureThresholdAnswer<>(
            3,
            "feature",
            "threshold"
        );


        when(adaptorAnswer.get(any())).thenReturn(0d);

        List<Double> selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)

        );
        Assertions.assertEquals(selected.size(), 3);

        when(adaptorAnswer.get(any())).thenReturn(1d);
        selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );
        Assertions.assertEquals(selected.size(), 3);

        when(adaptorAnswer.get(any())).thenReturn(2d);
        selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );
        Assertions.assertEquals(selected.size(), 2);

        when(adaptorAnswer.get(any())).thenReturn(3d);
        selected = filter.answer(
            options,
            extractor,
            mock(FishState.class),
            mock(Fisher.class)
        );
        Assertions.assertEquals(selected.size(), 1);
    }
}
