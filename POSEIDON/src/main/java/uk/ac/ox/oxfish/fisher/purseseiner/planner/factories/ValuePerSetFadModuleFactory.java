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

import uk.ac.ox.oxfish.fisher.purseseiner.planner.MarginalValueFadPlanningModule;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.ValuePerSetFadModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

public class ValuePerSetFadModuleFactory implements AlgorithmFactory<ValuePerSetFadModule>{



    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization =
            new SquaresMapDiscretizerFactory(6, 3);


    private String bannedXCoordinateBounds = "";
    private String bannedYCoordinateBounds = "";



    private DoubleParameter intercept = new FixedDoubleParameter(0);
    private DoubleParameter slope = new FixedDoubleParameter(1d);

    //Keep this at -1 to use the "old" intercept/slope method
    private DoubleParameter dampen = new FixedDoubleParameter( -1d);
    //0 gives no bias to the western waters. Increase this to increase the western bias
    private DoubleParameter westernBias = new FixedDoubleParameter(0);

    @Override
    public ValuePerSetFadModule apply(FishState state) {

        OwnFadSetDiscretizedActionGenerator optionsGenerator = new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(
                        discretization.apply(state)
                ),
                0
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

        if(dampen.equals(-1)){
            return new ValuePerSetFadModule(
                    optionsGenerator,
                    intercept.apply(state.getRandom()).doubleValue(),
                    slope.apply(state.getRandom()).doubleValue()
            );
        } else {
            return new ValuePerSetFadModule(
                    optionsGenerator,
                    0,1,
                    dampen.apply(state.getRandom()).doubleValue(),
                    westernBias.apply(state.getRandom()).doubleValue()

            );
        }
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(
            AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
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

    public DoubleParameter getIntercept() {
        return intercept;
    }

    public void setIntercept(DoubleParameter intercept) {
        this.intercept = intercept;
    }

    public DoubleParameter getSlope() {
        return slope;
    }

    public void setSlope(DoubleParameter slope) {
        this.slope = slope;
    }

    public DoubleParameter getDampen(){return dampen;}
    public void setDampen(DoubleParameter dampen){this.dampen=dampen;}

    public DoubleParameter getWesternBias(){return westernBias;}
    public void setWesternBias(DoubleParameter westernBias){this.westernBias=westernBias;}


}
