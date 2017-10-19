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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.CarryingCapacityDiffuser;
import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightLogisticInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 4/6/17.
 */
public class FromLeftToRightLogisticPlusClimateChangeFactory extends FromLeftToRightLogisticFactory {


    private DoubleParameter climateChangePercentageMovement = new FixedDoubleParameter(0.001);

    private  int northMigration = 1;

    private  int westMigration = 1;

    private int speciesAffectedByClimateChange = 0;

    /**
     * Applies this function to the given argument.
     *
     * @param model the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightLogisticInitializer apply(FishState model) {
        FromLeftToRightLogisticInitializer apply = super.apply(model);
        //add climate change
        model.registerStartable(new CarryingCapacityDiffuser(
                climateChangePercentageMovement.apply(model.getRandom()),
                northMigration,
                westMigration,
                speciesAffectedByClimateChange
        ));
        return apply;
    }

    /**
     * Getter for property 'climateChangePercentageMovement'.
     *
     * @return Value for property 'climateChangePercentageMovement'.
     */
    public DoubleParameter getClimateChangePercentageMovement() {
        return climateChangePercentageMovement;
    }

    /**
     * Setter for property 'climateChangePercentageMovement'.
     *
     * @param climateChangePercentageMovement Value to set for property 'climateChangePercentageMovement'.
     */
    public void setClimateChangePercentageMovement(DoubleParameter climateChangePercentageMovement) {
        this.climateChangePercentageMovement = climateChangePercentageMovement;
    }


    /**
     * Getter for property 'northMigration'.
     *
     * @return Value for property 'northMigration'.
     */
    public int getNorthMigration() {
        return northMigration;
    }

    /**
     * Setter for property 'northMigration'.
     *
     * @param northMigration Value to set for property 'northMigration'.
     */
    public void setNorthMigration(int northMigration) {
        this.northMigration = northMigration;
    }

    /**
     * Getter for property 'westMigration'.
     *
     * @return Value for property 'westMigration'.
     */
    public int getWestMigration() {
        return westMigration;
    }

    /**
     * Setter for property 'westMigration'.
     *
     * @param westMigration Value to set for property 'westMigration'.
     */
    public void setWestMigration(int westMigration) {
        this.westMigration = westMigration;
    }

    /**
     * Getter for property 'speciesAffectedByClimateChange'.
     *
     * @return Value for property 'speciesAffectedByClimateChange'.
     */
    public int getSpeciesAffectedByClimateChange() {
        return speciesAffectedByClimateChange;
    }

    /**
     * Setter for property 'speciesAffectedByClimateChange'.
     *
     * @param speciesAffectedByClimateChange Value to set for property 'speciesAffectedByClimateChange'.
     */
    public void setSpeciesAffectedByClimateChange(int speciesAffectedByClimateChange) {
        this.speciesAffectedByClimateChange = speciesAffectedByClimateChange;
    }
}
