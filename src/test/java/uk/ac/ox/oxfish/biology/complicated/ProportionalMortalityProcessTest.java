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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProportionalMortalityProcessTest
{


    @Test
    public void proportionalMortality() throws Exception {


        StructuredAbundance abundance = new StructuredAbundance(

                new double[]{100, 10, 1}
        );

        ProportionalMortalityProcess mortality = new ProportionalMortalityProcess(.1);

        mortality.cull(mock(Meristics.class),
                       false,
                       abundance,
                       365
                       );


        assertArrayEquals(
                abundance.asMatrix()[0],
                new double[]{90,9,.9},
                .001d
        );
    }


    @Test
    public void proportionalMortalityDailyForAYear() throws Exception {


        StructuredAbundance abundance = new StructuredAbundance(

                new double[]{100, 10, 1}
        );

        Meristics meristics = mock(Meristics.class);

        when(meristics.getNumberOfSubdivisions()).thenReturn(1);
        when(meristics.getNumberOfBins()).thenReturn(3);
        NaturalMortalityProcess mortality = new ProportionalMortalityProcess(.5
        );

        for(int i=0; i<365; i++) {
            mortality.cull(meristics,
                    false,
                    abundance,
                    1
            );
        }


        assertArrayEquals(
                abundance.asMatrix()[0],
                new double[]{50,5,.5},
                .001d
        );
    }
}