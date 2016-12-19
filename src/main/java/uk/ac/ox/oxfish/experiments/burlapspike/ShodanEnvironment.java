package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.policymakers.Controller;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Iterator;

/**
 * Created by carrknight on 12/19/16.
 */
public class ShodanEnvironment implements Environment
{



    public static int YEARS_PER_EPISODE = 20;


    FishState state;

    ExternalOpenCloseSeason shodan;


    public static final String ACTION_OPEN = "open";

    public static final String ACTION_CLOSE = "close";

    /**
     * Returns the current observation of the environment as a {@link State}.
     *
     * @return the current observation of the environment as a {@link State}.
     */
    @Override
    public ShodanStateOil currentObservation() {
            return ShodanStateOil.fromState(state);
    }

    /**
     * Returns the last reward returned by the environment
     *
     * @return the last reward returned by the environment
     */
    @Override
    public double lastReward() {
        if(state.getDay() <30)
            return 0d;
        else
        {
            Iterator<Double> record = state.getDailyDataSet().getColumn(
                    "Average Cash-Flow").descendingIterator();
            double reward = 0;
            for(int i=0; i<30; i++)
                reward += record.next();
            return reward;
        }

    }

    /**
     * Executes the specified action in this environment
     *
     * @param a the Action that is to be performed in this environment.
     * @return the resulting observation and reward transition from applying the given GroundedAction in this environment.
     */
    @Override
    public EnvironmentOutcome executeAction(Action a) {

        ShodanStateOil currentState =  currentObservation();


        if(a.actionName().equals(ACTION_OPEN))
            shodan.setOpen(true);
        else {
            assert a.actionName().equals(ACTION_CLOSE);
            shodan.setOpen(false);
        }
        //run the model for another 30 days
        for(int day=0; day<30; day++)
            state.schedule.step(state);

        /*
        System.out.println(a.actionName() + "  " + state.getFishers().get(0).getRegulation().allowedAtSea(null,state) +
                                   "   " + state.getMap().getPorts().iterator().next().getGasPricePerLiter()
        );
*/

        ShodanStateOil newState =  currentObservation();


        return new EnvironmentOutcome(
                currentState,
                a,
                newState,
                lastReward(),
                isInTerminalState()
        );

    }


    /**
     * Resets this environment to some initial state, if the functionality exists.
     */
    @Override
    public void resetEnvironment() {
        shodan = new ExternalOpenCloseSeason();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(100);

        scenario.setRegulation(new AlgorithmFactory<Regulation>() {
            @Override
            public Regulation apply(FishState state) {
                return shodan;
            }
        });

        state = new FishState(System.currentTimeMillis());
        state.attachAdditionalGatherers();
        state.setScenario(scenario);
        state.start();

        state.scheduleEveryXDay(new Steppable() {
            @Override
            public void step(SimState simState) {

                //change oil price
                for(Port port : state.getPorts())
                    port.setGasPricePerLiter(state.getDayOfTheYear()/1000d);
            }
        }, StepOrder.POLICY_UPDATE, 30);


    }

    /**
     * Returns whether the environment is in a terminal state that prevents further action by the agent.
     *
     * @return true if the current environment is in a terminal state; false otherwise.
     */
    @Override
    public boolean isInTerminalState() {
        return state.getYear()>=YEARS_PER_EPISODE;
    }

    public double totalReward(){
        double initialScore = 0;
        for(Double landing : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=landing;
        return initialScore;
    }



}
