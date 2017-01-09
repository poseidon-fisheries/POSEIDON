package uk.ac.ox.oxfish.model.regs;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
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
import uk.ac.ox.oxfish.model.data.Gatherer;

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

    private final QProvider qfunction;

    /**
     * receipt that we have started!
     */
    private Stoppable stoppable;


    public ShodanController(QProvider qFunction, ExternalOpenCloseSeason regulation) {
        policy = new GreedyQPolicy(qFunction);
        this.qfunction = qFunction;
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
        ShodanStateOil state = ShodanStateOil.fromState((FishState) simState);
        Action action = policy.action(state);
        StringBuilder qvalues = new StringBuilder();
        for(QValue qValue : qfunction.qValues(state))
            qvalues.append(qValue.a.actionName()).append(" -> ").append(qValue.q).append(" | ");

        System.out.println("shodan says: " +action + " , " + qvalues.toString());
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

        //add daily gatherer for policy
        model.getDailyDataSet().registerGatherer("Shodan Policy",
                                                  new Gatherer<FishState>() {
                                                      @Override
                                                      public Double apply(FishState state) {
                                                          return regulation.isOpen() ? 0d : 1d;
                                                      }
                                                  },
                                                  regulation.isOpen() ? 0d : 1d);
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
