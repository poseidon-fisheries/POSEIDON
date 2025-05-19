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

package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class CatchAtLengthTest {

    @Test
    public void histogramIsCorrect() {
        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setAllometricAlpha(new FixedDoubleParameter(0.02));
        factory.setAllometricBeta(new FixedDoubleParameter(2.94));
        factory.setMaxLengthInCm(new FixedDoubleParameter(81));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(0d));
        factory.setkYearlyParameter(new FixedDoubleParameter(0.4946723));
        factory.setNumberOfBins(82);


        GrowthBinByList meristics = factory.apply(mock(FishState.class));
        //   for(int i=0; i<meristics.getNumberOfBins(); i++)
        //         System.out.println(meristics.getLength(0,i));


        //data from R simulation
        int[] lenghts = new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 75, 81};
        int[] frequencies = new int[]{1, 1, 3, 2, 8, 15, 22, 20, 38, 37, 52, 61, 69, 69, 67, 73, 82, 66, 69, 58, 38, 49, 36, 20, 12, 16, 7, 5, 2, 1, 1};

        StructuredAbundance abundance = new StructuredAbundance(1, 82);
        for (int i = 0; i < lenghts.length; i++)
            abundance.asMatrix()[0][lenghts[i]] = frequencies[i];

        CatchAtLength catchAtLength = new CatchAtLength(
            abundance,
            new Species("test", meristics),
            5,
            20
        );
        System.out.println(Arrays.toString(catchAtLength.getCatchAtLength()));
    }
}
