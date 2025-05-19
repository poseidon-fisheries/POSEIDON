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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.TemporaryProtectedArea;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by carrknight on 7/27/16.
 */
public class TemporaryProtectedAreasFactory implements AlgorithmFactory<TemporaryProtectedArea> {


    private static ProtectedAreasOnly mpa = new ProtectedAreasOnly();
    /**
     * for each model I need to create starting mpas from scratch. Here I store
     * the stoppable as a receipt to make sure I create the MPAs only once
     */
    private final List<FishState> startReceipt = new LinkedList<>();
    private DoubleParameter startDay = new FixedDoubleParameter(0);
    private DoubleParameter duration = new FixedDoubleParameter(300);
    private List<StartingMPA> startingMPAs = new LinkedList<>();

    /**
     * Getter for property 'mpa'.
     *
     * @return Value for property 'mpa'.
     */
    public static ProtectedAreasOnly getMpa() {
        return mpa;
    }

    /**
     * Setter for property 'mpa'.
     *
     * @param mpa Value to set for property 'mpa'.
     */
    public static void setMpa(ProtectedAreasOnly mpa) {
        TemporaryProtectedAreasFactory.mpa = mpa;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public TemporaryProtectedArea apply(FishState state) {
        //if there are mpas to build and I haven't already done it, schedule yourself
        //at the start of the model to create the MPA
        //this makes sure both that the map is properly set up AND that it's only done once
        if (!startReceipt.contains(state) && !startingMPAs.isEmpty()) {
            state.registerStartable(new Startable() {
                @Override
                public void start(FishState model) {
                    for (StartingMPA mpa : startingMPAs)
                        mpa.buildMPA(model.getMap());
                }

                @Override
                public void turnOff() {

                }
            });
            startReceipt.add(state);
        }


        int startDay = (int) this.startDay.applyAsDouble(state.getRandom());
        return new TemporaryProtectedArea(
            startDay,
            startDay + (int) duration.applyAsDouble(state.getRandom())
        );
    }

    /**
     * Getter for property 'startDay'.
     *
     * @return Value for property 'startDay'.
     */
    public DoubleParameter getStartDay() {
        return startDay;
    }

    /**
     * Setter for property 'startDay'.
     *
     * @param startDay Value to set for property 'startDay'.
     */
    public void setStartDay(DoubleParameter startDay) {
        this.startDay = startDay;
    }

    /**
     * Getter for property 'duration'.
     *
     * @return Value for property 'duration'.
     */
    public DoubleParameter getDuration() {
        return duration;
    }

    /**
     * Setter for property 'duration'.
     *
     * @param duration Value to set for property 'duration'.
     */
    public void setDuration(DoubleParameter duration) {
        this.duration = duration;
    }

    /**
     * Getter for property 'startingMPAs'.
     *
     * @return Value for property 'startingMPAs'.
     */
    public List<StartingMPA> getStartingMPAs() {
        return startingMPAs;
    }

    /**
     * Setter for property 'startingMPAs'.
     *
     * @param startingMPAs Value to set for property 'startingMPAs'.
     */
    public void setStartingMPAs(List<StartingMPA> startingMPAs) {
        this.startingMPAs = startingMPAs;
    }

    /**
     * Getter for property 'startReceipt'.
     *
     * @return Value for property 'startReceipt'.
     */
    public List<FishState> getStartReceipt() {
        return startReceipt;
    }
}
