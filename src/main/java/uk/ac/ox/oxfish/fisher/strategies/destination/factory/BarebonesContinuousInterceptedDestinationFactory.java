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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GroupDummyExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.PortDistanceExtractor;
import uk.ac.ox.oxfish.fisher.strategies.destination.LogitDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.List;
import java.util.function.Function;

/**
 *  Adds dummy intercepts
 * Created by carrknight on 8/7/17.
 */
public class BarebonesContinuousInterceptedDestinationFactory extends BarebonesContinuousDestinationFactory {


    //these are from the california continuous 360 habit
    private double[] dummyIntercepts = new double[]{
            0,0.952581990919771,0.509322251168221,-16.018090230252,0.825325980430791,
            -16.758350120163,2.22550022667415,-0.138135388415003,-16.3278799379491,
            1.86916204885509,1.80244069634983,2.69254564577946,1.95866194001552,3.03465178784789,
            2.43391940965832,2.84185679984747,-0.115068606633148,3.61408775494346,1.70583832748963,
            3.10467795852511,1.33548927991838,-17.3507571259775,2.5902048442124,1.06849043721051,1.49731063611348,
            -0.740473040664187,2.38804627530531,-18.7033584944,-15.1932115106845,3.90488386910226,-0.0210724717780951,0.212637158885711
    };

    @NotNull
    @Override
    protected ObservationExtractor[][] buildExtractors(
            FishState state, MapDiscretization discretization, int areas, double[][] betas) {
        //get the extractors
        ObservationExtractor[][] extractors = new ObservationExtractor[betas.length][];
        ObservationExtractor[] commonExtractor = new ObservationExtractor[2+dummyIntercepts.length];
        commonExtractor[0] =
                buildHabitExtractor(discretization,
                                    getHabitPeriodInDays().apply(state.getRandom()).intValue());
        commonExtractor[1]=
                new PortDistanceExtractor();

        for(int j=0; j<areas; j++)
            commonExtractor[j+2] = new GroupDummyExtractor(j,discretization);

        for(int i=0; i<areas; i++)
            extractors[i] = commonExtractor;
        return extractors;
    }

    @Override
    protected double[][] buildBetas(FishState state, int areas, List<Integer> validAreas) {
        //as built originally
        double[][] originalBeta = super.buildBetas(state,areas,validAreas);
        //add all the dummies
        double beta[][] = new double[areas][2+dummyIntercepts.length];
        for(int i=0; i<areas; i++)
        {
            beta[i][0] = originalBeta[i][0];
            beta[i][1] = originalBeta[i][1];
            for(int j =2; j<beta[i].length; j++)
                beta[i][j] = 0;
            beta[i][i+2] = dummyIntercepts[i];
        }
        return beta;
    }

    /**
     * Getter for property 'dummyIntercepts'.
     *
     * @return Value for property 'dummyIntercepts'.
     */
    public double[] getDummyIntercepts() {
        return dummyIntercepts;
    }

    /**
     * Setter for property 'dummyIntercepts'.
     *
     * @param dummyIntercepts Value to set for property 'dummyIntercepts'.
     */
    public void setDummyIntercepts(double[] dummyIntercepts) {
        this.dummyIntercepts = dummyIntercepts;
    }
}
