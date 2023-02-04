/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.GreedyInsertionFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.MarginalValueFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

public class MarginalValueFadPlanningModuleFactory implements AlgorithmFactory<MarginalValueFadPlanningModule>{


    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
            new SquaresMapDiscretizerFactory(6, 3);

    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(0d);


    private String bannedXCoordinateBounds = "";
    private String bannedYCoordinateBounds = "";


    @Override
    public MarginalValueFadPlanningModule apply(FishState state) {

        OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(
                        discretization.apply(state)
                ),
                minimumValueFadSets.apply(state.getRandom())
        );
        if(!bannedXCoordinateBounds.isEmpty())
        {
            String[] coordinate = bannedXCoordinateBounds.split(",");
            if(coordinate.length==2)
            {
                double[] bannedX = Arrays.stream(bannedXCoordinateBounds.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                double[] bannedY = Arrays.stream(bannedYCoordinateBounds.split(","))
                        .mapToDouble(Double::parseDouble)
                        .toArray();
                optionsGenerator.setBannedGridBounds(bannedY,bannedX);
            }
        }

        return new MarginalValueFadPlanningModule(
                optionsGenerator
        );
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(
            AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }

    public String getBannedXCoordinateBounds() {
        return bannedXCoordinateBounds;
    }

    public void setBannedXCoordinateBounds(String bannedXCoordinateBounds) {
        this.bannedXCoordinateBounds = bannedXCoordinateBounds;
    }

    public String getBannedYCoordinateBounds() {
        return bannedYCoordinateBounds;
    }

    public void setBannedYCoordinateBounds(String bannedYCoordinateBounds) {
        this.bannedYCoordinateBounds = bannedYCoordinateBounds;
    }

}
