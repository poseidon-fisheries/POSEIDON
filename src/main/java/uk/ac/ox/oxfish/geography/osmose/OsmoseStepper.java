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

package uk.ac.ox.oxfish.geography.osmose;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import fr.ird.osmose.OsmoseSimulation;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedList;

/**
 * An object that takes care of coordinating and stepping OSMOSE biology within the FishState schedule
 * Created by carrknight on 6/26/15.
 */
public class OsmoseStepper implements Startable,Steppable{

    /**
     * how many times OSMOSE is supposed to step in a year
     */
    private final int stepsPerYearInOSMOSE;

    /**
     * how many times the fish-state is supposed to step each year
     */
    private final int stepsPerYearInFishState;

    /**
     * OSMOSE/FishState
     */
    private final double stepRatio;

    /**
     * the Osmose object
     */
    private final OsmoseSimulation osmoseSimulation;

    private double stepsLeft = 0;

    private final LinkedList<LocalOsmoseWithoutRecruitmentBiology> toReset = new LinkedList<>();

    /**
     * the stoppable receipt
     */
    private Stoppable receipt;

    /**
     * used for dithering
     */
    private final MersenneTwisterFast random;

    public OsmoseStepper(int stepsPerYearInFishState,
                         OsmoseSimulation osmoseSimulation,
                         MersenneTwisterFast random) {
        Preconditions.checkArgument(stepsPerYearInFishState > 0);
        Preconditions.checkArgument(osmoseSimulation.stepsPerYear() > 0);
        this.stepsPerYearInFishState = stepsPerYearInFishState;
        this.osmoseSimulation = osmoseSimulation;
        this.stepsPerYearInOSMOSE = osmoseSimulation.stepsPerYear();
        stepRatio = stepsPerYearInFishState/(double)stepsPerYearInOSMOSE;
        this.random = random;
        assert  stepRatio > 0;
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {
       receipt= model.scheduleEveryStep(this, StepOrder.BIOLOGY_PHASE);
        stepsLeft = stepRatio;
    }


    @Override
    public void step(SimState simState)
    {
        stepsLeft--;
        stepsLeft = FishStateUtilities.round(stepsLeft);
        //notice the dithering when steps don't match really well
        if(stepsLeft<=0 || (stepsLeft < 1 && random.nextBoolean(1-stepsLeft)))
        {
            Log.trace("OSMOSE step!");
            osmoseSimulation.oneStep();
            stepsLeft=stepRatio; //reset
            toReset.forEach(LocalOsmoseWithoutRecruitmentBiology::osmoseStep);
        }


        assert  stepsLeft > 0;
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
    }

    public int getStepsPerYearInOSMOSE() {
        return stepsPerYearInOSMOSE;
    }

    public int getStepsPerYearInFishState() {
        return stepsPerYearInFishState;
    }

    public LinkedList<LocalOsmoseWithoutRecruitmentBiology> getToReset() {
        return toReset;
    }
}
