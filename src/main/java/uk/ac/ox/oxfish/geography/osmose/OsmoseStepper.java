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

    private final LinkedList<LocalOsmoseBiology> toReset = new LinkedList<>();

    /**
     * the stoppable receipt
     */
    private Stoppable receipt;

    /**
     * used for dithering
     */
    private final MersenneTwisterFast random;

    public OsmoseStepper(int stepsPerYearInFishState, OsmoseSimulation osmoseSimulation,
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
            toReset.forEach(LocalOsmoseBiology::osmoseStep);
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

    public LinkedList<LocalOsmoseBiology> getToReset() {
        return toReset;
    }
}
