package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.HourlyProfitInTripObjective;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AmateurishApproximateDynamicProgram;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.DefaultBeamHillClimbing;

import java.util.HashMap;

/**
 * This is my first attempt at a workable approximate dynamic programming agent.
 * The key idea is that everyone shares the same program (so that learning of the parameters is faster). <br>
 *     Decisions are taken on the "should I go out" question. Learning is done next time the trip starts (no reason to be a tripListener)
 * Created by carrknight on 10/13/16.
 */
public class AmateurishDynamicStrategy implements DestinationStrategy, DepartingStrategy {


    /**
     * what were the states like last decision taken?
     */
    private final HashMap<Fisher,double[]> previousFeatures = new HashMap<>();

    /**
     * what was the last decision taken? as action index and utility prediction
     */
    private final HashMap<Fisher,Pair<Integer,Double>> previousDecisions = new HashMap<>();



    /**
     * the crap dynamic program that takes decisions
     */
    private final AmateurishApproximateDynamicProgram program;

    /**
     * array with all the sensor that turns the current state into a number
     */
    private final Sensor<Fisher,Double> featuresExtractor[];


    /**
     * the strategy doing all the navigation
     */
    private final HashMap<Fisher,FavoriteDestinationStrategy> delegates = new HashMap<>();

    /**
     * the algorithm we use to actually explore/exploit/imitate
     */
    private final DefaultBeamHillClimbing explorer;

    /**
     * reference to the action possibles
     */
    public final static ActionType[] actions = ActionType.values();


    private final Counter actionsTaken;
    /**
     * how much the future matters
     */
    private final double discountRate;

    /**
     * probability of taking a random decision instead of the best decision
     */
    private double noiseRate;

    private final Supplier<FavoriteDestinationStrategy> delegateGenerator;

    public AmateurishDynamicStrategy(
            double learningRate, double noiseRate,
            NauticalMap map, MersenneTwisterFast random,
            int explorationDelta, final double discountRate,
            Sensor<Fisher, Double>... featuresExtractor) {
        this.program = new AmateurishApproximateDynamicProgram(ActionType.values().length,
                                                               featuresExtractor.length,
                                                               learningRate
        );

        this.delegateGenerator = new Supplier<FavoriteDestinationStrategy>() {
            @Override
            public FavoriteDestinationStrategy get() {
                return new FavoriteDestinationStrategy(map,random);
            }
        };
        this.noiseRate = noiseRate;
        this.explorer = new DefaultBeamHillClimbing(explorationDelta,50);

        this.featuresExtractor = featuresExtractor;
        Preconditions.checkArgument(featuresExtractor.length>0);


        actionsTaken = new Counter(IntervalPolicy.EVERY_DAY);
        for(ActionType type : actions)
            actionsTaken.addColumn(type.name());
        this.discountRate = discountRate;
    }


    private double[] featurize(Fisher fisher)
    {
        double[] features = new double[featuresExtractor.length];
        for(int i =0; i< features.length; i++) {
            features[i] = featuresExtractor[i].scan(fisher);
            assert Double.isFinite(features[i]);
        }
        return features;
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random)
    {

        //always start by turning the new state into features
        double[] currentFeatures = featurize(fisher);

        //what is the optimal decision?
        Pair<Integer,Double> currentDecision = program.chooseBestAction(currentFeatures);
        //with a small chance try something completely bonkers
        if(random.nextBoolean(noiseRate)) {
            int randomAction = random.nextInt(actions.length);
            currentDecision = new Pair<>(randomAction, program.judgeAction(randomAction,currentFeatures));
        }


        //learn from old step
        double[] oldFeatures = previousFeatures.get(fisher);
        //if you have taken a decision before then learn from it!
        if(oldFeatures != null)
        {
            //pick up old action
            Pair<Integer,Double> previousDecision = previousDecisions.get(fisher);
            double reward;
            //if you stayed home, the reward is 0
            if(previousDecision.getFirst()==ActionType.STAY_HOME.ordinal())
                reward = 0;
            else
                //otherwise the reward is profits per hour that trip
                reward = fisher.getLastFinishedTrip().getProfitPerHour(true);
            assert Double.isFinite(reward);

            //the observation is the immediate reward + expected value of the best decision we take next
            //strictly speaking it should be the max of both the reward and expected value but the way
            //we structure this problem reward is not a function of the action taken (as it is just the end of the trip)
            //so the reward drops out of the maximization.
            double observation = reward + discountRate * currentDecision.getSecond();

            program.updateAction(previousDecision.getFirst(),observation,oldFeatures);
        }


        //now it's time to act.
        actionsTaken.count(actions[currentDecision.getFirst()].name(),1);
        FavoriteDestinationStrategy delegate = delegates.get(fisher);
        switch (actions[currentDecision.getFirst()])
        {
            case STAY_HOME:
                break;
            case EXPLOIT:
                //this actually doesn't change anything but let's keep it in case I go insane and exploit starts meaning something else
                delegate.setFavoriteSpot(explorer.exploit(random,fisher,Double.NaN,delegate.getFavoriteSpot()));
                break;
            case EXPLORE:
                delegate.setFavoriteSpot(explorer.randomize(random,fisher,Double.NaN,delegate.getFavoriteSpot()));
                break;
            case IMITATE:
                //i force myself to imitate, to do so I put my current "utility" to basically - infinity
                //so that I will imitate, unless there is nobody to imitate
                delegate.setFavoriteSpot(explorer.imitate(random, fisher,
                                                          -Double.MAX_VALUE,
                                                          delegate.getFavoriteSpot(),
                                                          fisher.getDirectedFriends(),
                                                          new HourlyProfitInTripObjective(true),
                                                          new Sensor<Fisher, SeaTile>() {
                                                              @Override
                                                              public SeaTile scan(Fisher system) {
                                                                  TripRecord lastFinishedTrip = system.getLastFinishedTrip();
                                                                  if(lastFinishedTrip== null)
                                                                      return null;
                                                                  else
                                                                      return lastFinishedTrip.getMostFishedTileInTrip();
                                                              }
                                                          }
                ).getFirst());
                break;
            default:
                throw new RuntimeException("Not supposed to be here!");

        }


        //store for future use!
        previousFeatures.put(fisher,currentFeatures);
        previousDecisions.put(fisher,currentDecision);

        //finally return yes or no to the original question
        if(currentDecision.getFirst()== ActionType.STAY_HOME.ordinal())
            return false;
        else
            return true;
    }

    /**
     * Getter for property 'noiseRate'.
     *
     * @return Value for property 'noiseRate'.
     */
    public double getNoiseRate() {
        return noiseRate;
    }

    /**
     * Setter for property 'noiseRate'.
     *
     * @param noiseRate Value to set for property 'noiseRate'.
     */
    public void setNoiseRate(double noiseRate) {
        this.noiseRate = noiseRate;
    }


    /**
     * returns the value function of being at a state (summarized by the state variables) and taking an action (identified by the action number)
     * @param action the action id
     * @param stateFeatures the measurements summarising the state we are at
     * @return a number, the higher the better
     */
    public double judgeAction(int action, double... stateFeatures) {
        return program.judgeAction(action, stateFeatures);
    }

    /**
     * go through all the actions and choose the one with highest value given the current features
     * @param stateFeatures the features of the state
     * @return the action commanding the highest value and the value function
     */
    public Pair<Integer, Double> chooseBestAction(double... stateFeatures) {
        return program.chooseBestAction(stateFeatures);
    }

    /**
     * Getter for property 'linearParameters'.
     *
     * @return Value for property 'linearParameters'.
     */
    public double[][] getLinearParameters() {
        return program.getLinearParameters();
    }


    boolean started = false;

    @Override
    public void start(FishState model, Fisher fisher) {
        FavoriteDestinationStrategy delegate = delegateGenerator.get();
        delegates.put(fisher, delegate);
        delegate.start(model,fisher);
        if(!started) {
            explorer.start(model, fisher, delegate.getFavoriteSpot());
            actionsTaken.start(model);
        }
        started =true;
    }


    boolean turnedOff = false;
    @Override
    public void turnOff(Fisher fisher) {

        if(started) {
            FavoriteDestinationStrategy delegate = delegates.remove(fisher);
            if(delegate != null)
                delegate.turnOff(fisher);
            if (!turnedOff) {
                actionsTaken.turnOff();
            }
            turnedOff = true;
        }
    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {
        return delegates.get(fisher).chooseDestination(fisher,random,model,currentAction);
    }

    private enum ActionType
    {
        EXPLORE,

        EXPLOIT,

        IMITATE,

        STAY_HOME

    }

    /**
     * Getter for property 'actionsTaken'.
     *
     * @return Value for property 'actionsTaken'.
     */
    public Counter getActionsTaken() {
        return actionsTaken;
    }
}
