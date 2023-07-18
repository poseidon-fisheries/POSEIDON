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

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/6/17.
 */
public class ProportionalAgingAndRecruitmentProcessTest {


    @Test
    public void halfAge() throws Exception {

        final Species species = mock(Species.class);
        when(species.getNumberOfBins()).thenReturn(3);
        final double[] male = {10, 20, 30};
        final double[] female = {100, 200, 300};
        final StructuredAbundance abundance = new StructuredAbundance(male, female);

        final ProportionalAgingProcess process = new ProportionalAgingProcess(new FixedDoubleParameter(0.5d));

        final AbundanceLocalBiology bio = mock(AbundanceLocalBiology.class);
        when(bio.getAbundance(species)).thenReturn(abundance);


        final FishState model = mock(FishState.class);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        process.ageLocally(bio, species, model, true, 365);

        Assertions.assertArrayEquals(male, new double[]{5, 15, 25}, .001);
        Assertions.assertArrayEquals(female, new double[]{50, 150, 250}, .001);

    }


}