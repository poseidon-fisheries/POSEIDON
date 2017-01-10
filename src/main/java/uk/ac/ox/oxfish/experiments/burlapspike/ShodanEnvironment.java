package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.ExternalOpenCloseSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Iterator;

/**
 * Created by carrknight on 12/19/16.
 */
public class ShodanEnvironment implements Environment
{



    public static int YEARS_PER_EPISODE = 20;


    private FishState state;

    private ExternalOpenCloseSeason shodan;


    public static final String ACTION_OPEN = "open";

    public static final String ACTION_CLOSE = "close";
    private final PrototypeScenario scenario;
    private Steppable additionalSteppable;

    /**
     * year when the episode ends, if this is negative the scenario never ends!
     */
    private final int lastYear;


    /**
     * create an environment object that stores the prototype scenario
     * @param scenario
     * @param additionalSteppable
     * @param lastYear if negative the episode never ends
     */
    public ShodanEnvironment(PrototypeScenario scenario, Steppable additionalSteppable, int lastYear) {
        this.scenario = scenario;
        this.additionalSteppable = additionalSteppable;
        this.lastYear = lastYear;
    }

    public ShodanEnvironment(final PrototypeScenario scenario, final Steppable additionalSteppable) {
        this(scenario,additionalSteppable,YEARS_PER_EPISODE);
    }




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
        if(state.getDay() <29)
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
        //lspiRun the model for another 30 days
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
        resetEnvironment(System.currentTimeMillis());


    }


    public void resetEnvironment(long seed) {
        shodan = new ExternalOpenCloseSeason();

        scenario.setRegulation(new AlgorithmFactory<Regulation>() {
            @Override
            public Regulation apply(FishState state) {
                return shodan;
            }
        });

        state = new FishState(seed);
        state.attachAdditionalGatherers();
        state.setScenario(scenario);
        state.start();


        state.scheduleEveryXDay(additionalSteppable, StepOrder.POLICY_UPDATE, 30);

        state.getDailyDataSet().registerGatherer("Shodan Policy",
                                                 new Gatherer<FishState>() {
                                                     @Override
                                                     public Double apply(FishState state) {
                                                         if(shodan.isOpen()) return  0d;
                                                         else
                                                             return 1d;
                                                     }
                                                 },shodan.isOpen() ? 0 : 1);


    }
    /**
     * Returns whether the environment is in a terminal state that prevents further action by the agent.
     *
     * @return true if the current environment is in a terminal state; false otherwise.
     */
    @Override
    public boolean isInTerminalState() {
        return lastYear >0 && state.getYear()>=lastYear;
    }

    public double totalReward(){
        double initialScore = 0;
        for(Double cashflow : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
            initialScore+=cashflow;
        return initialScore;
    }


    /**
     * Getter for property 'state'.
     *
     * @return Value for property 'state'.
     */
    public FishState getState() {
        return state;
    }



    /**
     * Getter for property 'shodan'.
     *
     * @return Value for property 'shodan'.
     */
    public ExternalOpenCloseSeason getShodan() {
        return shodan;
    }


}
