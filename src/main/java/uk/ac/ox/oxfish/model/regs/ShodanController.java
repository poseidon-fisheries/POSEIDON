package uk.ac.ox.oxfish.model.regs;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.valuefunction.QProvider;
import burlap.mdp.core.action.Action;
import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.experiments.burlapspike.ShodanEnvironment;
import uk.ac.ox.oxfish.experiments.burlapspike.ShodanStateOil;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * A method that is stepped every month and opens/close a season with given QProvider
 * Created by carrknight on 1/3/17.
 */
public class ShodanController implements Steppable,Startable{


    /**
     * the object that picks an action given a controller
     */
    private final GreedyQPolicy policy;


    /**
     * the regulation object we manipulate
     */
    private final  ExternalOpenCloseSeason regulation;

    /**
     * receipt that we have started!
     */
    private Stoppable stoppable;


    public ShodanController(QProvider qFunction, ExternalOpenCloseSeason regulation) {
        policy = new GreedyQPolicy(qFunction);
        this.regulation = regulation;
    }

    /**
     * open or close the world
     * @param simState
     */
    @Override
    public void step(SimState simState)
    {

        //ask the policy what should be done
        Action action = policy.action(ShodanStateOil.fromState((FishState) simState));
        System.out.println("shodan says: " +action);
        //actuate to the fishstate
        if(action.actionName().equals(ShodanEnvironment.ACTION_OPEN))
            regulation.setOpen(true);
        else
            {
            assert action.actionName().equals(ShodanEnvironment.ACTION_CLOSE);
            regulation.setOpen(false);
        }

    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkState(stoppable==null, "shodan has already started");
        //schedule yourself every month (but skip the first day since you need 30 days of data each step)
        model.scheduleOnceInXDays(new Steppable() {
            @Override
            public void step(SimState simState) {
                stoppable = model.scheduleEveryXDay(ShodanController.this, StepOrder.POLICY_UPDATE, 30);

            }
        },StepOrder.POLICY_UPDATE,1);
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        Preconditions.checkArgument(stoppable==null);
        stoppable.stop();
    }

    /**
     * Getter for property 'policy'.
     *
     * @return Value for property 'policy'.
     */
    public GreedyQPolicy getPolicy() {
        return policy;
    }

    /**
     * Getter for property 'regulation'.
     *
     * @return Value for property 'regulation'.
     */
    public ExternalOpenCloseSeason getRegulation() {
        return regulation;
    }
}
