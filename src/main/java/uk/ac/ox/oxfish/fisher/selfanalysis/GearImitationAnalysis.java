package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.DockingListener;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;

import java.util.List;

/**
 * Run every two months, check how are you doing, then check a friend. If he is doing better than you have a small probability
 * of copying their gear (without paying the price)
 * Created by carrknight on 8/4/15.
 */
public class GearImitationAnalysis implements FisherStartable
{

    public static final Actuator<Gear> DEFAULT_GEAR_ACTUATOR = new Actuator<Gear>() {
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
     * the algorithm doing the exploration-imitation thing
     */
    private final Adaptation<Gear> algorithm;


    private  Fisher fisher;

    /**
     * the default actuator resets predictors and changes gear only at the end of the trip, use this
     * if you want to use something different
     * @param probabilityRandomizing the probability of randomizing
     * @param probabilityImitating the probability of copying your neighbor
     * @param gearAvailable the list of gear that is selectable
     * @param objective the objective function to use to judge the imitation
     */
    public GearImitationAnalysis(
            double probabilityRandomizing, double probabilityImitating,
            List<Gear> gearAvailable, ObjectiveFunction<Fisher> objective,
            Actuator<Gear> actuator)
    {
        algorithm = new Adaptation<>(fisher1 -> true,
                                     new DiscreteRandomAlgorithm<>(gearAvailable),
                                     actuator,
                                     Fisher::getGear,
                                     objective,probabilityRandomizing,probabilityImitating);



    }

    public GearImitationAnalysis(
            double probabilityRandomizing, double probabilityImitating,
            List<Gear> gearAvailable, ObjectiveFunction<Fisher> objective)
    {
        this(probabilityRandomizing,probabilityImitating,gearAvailable,objective,
             DEFAULT_GEAR_ACTUATOR);
    }


    @Override
    public void start(FishState model, Fisher fisher)
    {
        fisher.addBiMonthlyAdaptation(algorithm);
        this.fisher = fisher; //remember in order to remove yourself if turned off separately
    }

    /**
     * tell the startable to turnoff,
     */
    public void turnOff() {
        fisher.removeBiMonthlyAdaptation(algorithm);

    }



    public static void attachMileageGearAdaptationToEachFisher(List<Fisher> fishers, FishState model,
                                                               final CashFlowObjective objectiveFunction,
                                                               final double minTrawlingSpeed,
                                                               final double maxTrawlingSpeed)
    {
        //first add data gatherers
        model.getDailyDataSet().registerGatherer("Thrawling Fuel Consumption", state -> {
            double size =state.getFishers().size();
            if(size == 0)
                return Double.NaN;
            else
            {
                double total = 0;
                for(Fisher fisher1 : state.getFishers())
                    total+= ((RandomCatchabilityTrawl) fisher1.getGear()).getTrawlSpeed();
                return total/size;
            }
        }, Double.NaN);


        for(int i=0; i<model.getSpecies().size(); i++)
        {
            final int finalI = i;
            model.getDailyDataSet().registerGatherer("Trawling Efficiency for Species " + i,
                                                     state -> {
                                                         double size = state.getFishers().size();
                                                         if (size == 0)
                                                             return Double.NaN;
                                                         else {
                                                             double total = 0;
                                                             for (Fisher fisher1 : state.getFishers())
                                                                 total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[finalI];
                                                             return total / size;
                                                         }
                                                     }, Double.NaN);
        }


        final double shock = 0.05*(maxTrawlingSpeed-minTrawlingSpeed);
        assert  shock >=0;
        //add adaptation
        for(Fisher fisher : fishers)
        {

            Adaptation<RandomCatchabilityTrawl> mileagedaptation = new Adaptation<>(
                    fisher1 -> true,
                    new BeamHillClimbing<RandomCatchabilityTrawl>() {
                        @Override
                        public RandomCatchabilityTrawl randomStep(
                                FishState state, MersenneTwisterFast random, Fisher fisher,
                                RandomCatchabilityTrawl current) {
                            double currentShock = random.nextDouble() * shock;
                            if (random.nextBoolean())
                                currentShock -= currentShock;
                            double newMileage = current.getTrawlSpeed() + currentShock;
                            newMileage = Math.max(newMileage, minTrawlingSpeed);
                            newMileage = Math.min(newMileage, maxTrawlingSpeed);
                            return new RandomCatchabilityTrawl(
                                    current.getCatchabilityMeanPerSpecie(),
                                    current.getCatchabilityDeviationPerSpecie(),
                                    newMileage
                            );
                        }
                    }, (fisher1, change, model1) -> fisher1.setGear(change),
                    fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                    objectiveFunction, .2, .6);



            model.registerStartable(new FisherStartable() {
                @Override
                public void start(FishState model, Fisher fisher) {
                    fisher.addBiMonthlyAdaptation(mileagedaptation);
                }

                @Override
                public void turnOff() {
                    fisher.removeBiMonthlyAdaptation(mileagedaptation);
                }
            }, fisher);
        }


    }


    /**
     * utility function to add the imitation analysis to every fisher in the group
     */
    public static void attachGearAnalysisToEachFisher(
            List<Fisher> fishers, FishState model, final List<Gear> randomGear,
            final CashFlowObjective objectiveFunction)
    {
        //add analysis
        for(Fisher fisher : fishers)
        {
            GearImitationAnalysis analysis = new GearImitationAnalysis(0.05, .25, randomGear,
                                                                       objectiveFunction);

            model.registerStartable(analysis,fisher);
        }


        model.getDailyDataSet().registerGatherer("Thrawling Fuel Consumption", state -> {
            double size =state.getFishers().size();
            if(size == 0)
                return Double.NaN;
            else
            {
                double total = 0;
                for(Fisher fisher1 : state.getFishers())
                    total+= ((RandomCatchabilityTrawl) fisher1.getGear()).getTrawlSpeed();
                return total/size;
            }
        }, Double.NaN);


        for(int i=0; i<model.getSpecies().size(); i++)
        {
            final int finalI = i;
            model.getDailyDataSet().registerGatherer("Trawling Efficiency for Species " + i,
                                                     state -> {
                                                         double size = state.getFishers().size();
                                                         if (size == 0)
                                                             return Double.NaN;
                                                         else {
                                                             double total = 0;
                                                             for (Fisher fisher1 : state.getFishers())
                                                                 total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[finalI];
                                                             return total / size;
                                                         }
                                                     }, Double.NaN);
        }
    }


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

            Adaptation<Hold> holdAdaptation = new Adaptation<>(
                    fisher1 -> true,
                    new BeamHillClimbing<Hold>() {
                        @Override
                        public Hold randomStep(
                                FishState state, MersenneTwisterFast random, Fisher fisher, Hold current) {
                            return new Hold(fisher.getMaximumHold() * (.8 + .4 * random.nextDouble()),
                                            species);
                        }
                    }, (fisher1, change, model1) -> fisher1.changeHold(change),
                    fisher1 -> {
                        //create a new hold for scanning. Helps with safety plus we can't get Fisher hold
                        return new Hold(fisher1.getMaximumHold(), species);
                    }, new CashFlowObjective(60), .15, .6);



            model.registerStartable(new FisherStartable() {
                @Override
                public void start(FishState model, Fisher fisher) {
                    fisher.addBiMonthlyAdaptation(holdAdaptation);
                }

                @Override
                public void turnOff() {
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
            Adaptation<FixedProbabilityDepartingStrategy> departingChance
                    = new Adaptation<>(
                    fisher1 -> true,
                    new BeamHillClimbing<FixedProbabilityDepartingStrategy>() {
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
                    },
                    (fisher1, change, model1) -> fisher1.setDepartingStrategy(change),
                    fisher1 -> ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()),
                    new CashFlowObjective(60),
                    explorationProbability, imitationProbability
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



    public Adaptation<Gear> getAlgorithm() {
        return algorithm;
    }

    public Fisher getFisher() {
        return fisher;
    }
}
