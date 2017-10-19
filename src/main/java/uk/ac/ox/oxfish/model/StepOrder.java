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

package uk.ac.ox.oxfish.model;


public enum StepOrder {


    DAWN(false),

    /**
     * fisher act
     */
    FISHER_PHASE(true),

    /**
     * biome regenerates
     */
    BIOLOGY_PHASE(true),


    POLICY_UPDATE(true),

    /**
     * data is stored in TimeSeries objects
     */
    DAILY_DATA_GATHERING(false),

    /**
     * data is stored in TimeSeries objects
     */
    YEARLY_DATA_GATHERING(false),

    /**
     * aggregate data usually access individual data that has just been stored, so it has to happen later
     */
    AGGREGATE_DATA_GATHERING(false),

    /**
     * counters get reset to 0. Ready to be written over
     */
    DATA_RESET(false),

    /**
     * exogenous forces that act when the model has stepped can be used here (a simple GA algorithm for example)
     */
    AFTER_DATA(true);


    private final boolean toRandomize;

    StepOrder(boolean shouldBeRandomized) {
        this.toRandomize = shouldBeRandomized;
    }


    public boolean isToRandomize() {
        return toRandomize;
    }
}
