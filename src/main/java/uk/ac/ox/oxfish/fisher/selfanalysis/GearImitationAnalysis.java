package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.DockingListener;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;

import java.util.List;
import java.util.function.Predicate;

/**
 * Run every two months, check how are you doing, then check a friend. If he is doing better than you have a small probability
 * of copying their gear (without paying the price)
 * Created by carrknight on 8/4/15.
 */
public class GearImitationAnalysis
{

    public static final Actuator<Fisher,Gear> DEFAULT_GEAR_ACTUATOR = new Actuator<Fisher,Gear>() {
        @Override
        public void apply(Fisher fisher1, Gear change, FishState model) {
            if (Log.TRACE)
                Log.trace(fisher1 + " is about to change gear");
            //predictions are wrong: reset at the end of the trip
            fisher1.addDockingListener(new DockingListener() {
                boolean active = true;
                @Override
                public void dockingEvent(Fisher fisher, Port port)
                {
                    if(!active)
                        return;
                    fisher1.setGear(change.makeCopy());
                    Log.trace(fisher1 + " has changed gear and will reset its predictor");
                    fisher1.resetDailyCatchesPredictors();
                    active=false;
                    DockingListener outer = this;
                    //schedule to remove the listener
                    model.scheduleOnce((Steppable) simState -> fisher1.removeDockingListener(outer), StepOrder.DAWN);


                }
            });



        }
    };








    /**
     * creates a bimonthly adaptation to increase or decrease the size of the hold available for each fisher
     * @param fishers a list of fisher to adapt
     * @param model the fishstate
     */
    public static void attachHoldSizeAnalysisToEachFisher(
            List<Fisher> fishers, FishState model)
    {

        int species = model.getBiology().getSize();

        //add analysis
        for(Fisher fisher : fishers)
        {

            ExploreImitateAdaptation<Hold> holdAdaptation = new ExploreImitateAdaptation<>(
                    fisher1 -> true,
                    new BeamHillClimbing<Hold>(
                            new RandomStep<Hold>() {
                                public Hold randomStep(
                                        FishState state, MersenneTwisterFast random, Fisher fisher, Hold current) {
                                    return new Hold(fisher.getMaximumHold() * (.8 + .4 * random.nextDouble()),
                                                    species);
                                }
                            }

                    ), (fisher1, change, model1) -> fisher1.changeHold(change),
                    fisher1 -> {
                        //create a new hold for scanning. Helps with safety plus we can't get Fisher hold
                        return new Hold(fisher1.getMaximumHold(), species);
                    }, new CashFlowObjective(60), .15, .6, new Predicate<Hold>() {
                        @Override
                        public boolean test(Hold a) {
                            return true;
                        }
                    });



            model.registerStartable(new FisherStartable() {
                @Override
                public void start(FishState model, Fisher fisher) {
                    fisher.addBiMonthlyAdaptation(holdAdaptation);
                }

                @Override
                public void turnOff(Fisher fisher) {
                    fisher.removeBiMonthlyAdaptation(holdAdaptation);
                }
            }, fisher);
        }


        model.getDailyDataSet().registerGatherer("Holding Size", state -> {
            double size =state.getFishers().size();
            if(size == 0)
                return Double.NaN;
            else
            {
                double total = 0;
                for(Fisher fisher1 : state.getFishers())
                    total+=  fisher1.getMaximumHold();
                return total/size;
            }
        }, Double.NaN);



    }


    public static void attachGoingOutProbabilityToEveryone(
            List<Fisher> fishers,
            FishState model, final double shockSize, final double explorationProbability,
            final double imitationProbability)
    {
        for(Fisher fisher : fishers)
        {
            ExploreImitateAdaptation<FixedProbabilityDepartingStrategy> departingChance
                    = new ExploreImitateAdaptation<>(
                    fisher1 -> true,
                    new BeamHillClimbing<FixedProbabilityDepartingStrategy>(
                            new RandomStep<FixedProbabilityDepartingStrategy>() {
                                @Override
                                public FixedProbabilityDepartingStrategy randomStep(
                                        FishState state, MersenneTwisterFast random, Fisher fisher,
                                        FixedProbabilityDepartingStrategy current) {
                                    double probability = current.getProbabilityToLeavePort();
                                    double shock = (2 *shockSize) * random.nextDouble() - shockSize;
                                    probability = probability * (1+shock);
                                    probability = Math.min(Math.max(0, probability), 1);
                                    return new FixedProbabilityDepartingStrategy(probability, false);
                                }
                            }
                    ),
                    (fisher1, change, model1) -> fisher1.setDepartingStrategy(change),
                    fisher1 -> ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()),
                    new CashFlowObjective(60),
                    explorationProbability, imitationProbability, new Predicate<FixedProbabilityDepartingStrategy>() {
                        @Override
                        public boolean test(FixedProbabilityDepartingStrategy a) {
                            return true;
                        }
                    }
            );
            fisher.addBiMonthlyAdaptation(departingChance);


        }
        model.getDailyDataSet().registerGatherer("Probability to leave port", state1 -> {
            double size = state1.getFishers().size();
            if (size == 0)
                return Double.NaN;
            else {
                double total = 0;
                for (Fisher fisher1 : state1.getFishers())
                    total += ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()).
                            getProbabilityToLeavePort();
                return total / size;
            }
        }, Double.NaN);


    }



}
