package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityThrawl;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.utility.maximization.Actuator;
import uk.ac.ox.oxfish.utility.maximization.Adaptation;
import uk.ac.ox.oxfish.utility.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.maximization.Sensor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Run every two months, check how are you doing, then check a friend. If he is doing better than you have a small probability
 * of copying their gear (without paying the price)
 * Created by carrknight on 8/4/15.
 */
public class GearImitationAnalysis implements FisherStartable
{

    /**
     * the algorithm doing the exploration-imitation thing
     */
    private final Adaptation<Gear> algorithm;


    private  Fisher fisher;

    public GearImitationAnalysis(
            double probabilityRandomizing, double probabilityImitating,
            List<Gear> gearAvailable, ObjectiveFunction<Fisher> objective) {
        algorithm = new Adaptation<>(fisher1 -> true,
                                                           new DiscreteRandomAlgorithm<Gear>(gearAvailable),
                                                           (fisher1, change,model) -> fisher1.setGear(change.cloneGear()),
                                                           Fisher::getGear,
                                                           objective,probabilityRandomizing,probabilityImitating);



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




    /**
     * utility function to add the imitation analysis to every fisher in the group
     */
    public static void attachGearAnalysisToEachFisher(
            List<Fisher> fishers, FishState model, final List<Gear> randomGear)
    {
        //add analysis
        for(Fisher fisher : fishers)
        {
            GearImitationAnalysis analysis = new GearImitationAnalysis(0, .25, randomGear,
                                                                       new CashFlowObjective(60));

            model.registerStartable(analysis,fisher);
        }


        model.getDailyDataSet().registerGatherer("Thrawling Fuel Consumption", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                double size =state.getFishers().size();
                if(size == 0)
                    return Double.NaN;
                else
                {
                    double total = 0;
                    for(Fisher fisher : state.getFishers())
                        total+= ((RandomCatchabilityThrawl) fisher.getGear()).getThrawlSpeed();
                    return total/size;
                }
            }
        },Double.NaN);


        model.getDailyDataSet().registerGatherer("Thrawling Efficiency", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                double size = state.getFishers().size();
                if (size == 0)
                    return Double.NaN;
                else {
                    double total = 0;
                    for (Fisher fisher : state.getFishers())
                        total += ((RandomCatchabilityThrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0];
                    return total / size;
                }
            }
        }, Double.NaN);
    }


    /**
     * creates a bimonthly adaptation to increase or decrease the size of the hold available for each fisher
     * @param fishers
     * @param model
     */
    public static void attachHoldSizeAnalysisToEachFisher(
            List<Fisher> fishers, FishState model)
    {

        int species = model.getBiology().getSize();

        //add analysis
        for(Fisher fisher : fishers)
        {

            Adaptation<Hold> holdAdaptation = new Adaptation<Hold>(
                    fisher1 -> true,
                    new BeamHillClimbing<Hold>() {
                @Override
                public Hold randomStep(
                        FishState state, MersenneTwisterFast random, Fisher fisher, Hold current) {
                    return new Hold(fisher.getMaximumLoad() * (.8 + .4 * random.nextDouble()),
                                    species);
                }
            }, (fisher1, change, model1) -> fisher1.setHold(change),
                    fisher1 -> {
                //create a new hold for scanning. Helps with safety plus we can't get Fisher hold
                return new Hold(fisher1.getMaximumLoad(),species);
            },new CashFlowObjective(60),.15,.6);



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


        model.getDailyDataSet().registerGatherer("Holding Size", new Function<FishState, Double>() {
            @Override
            public Double apply(FishState state) {
                double size =state.getFishers().size();
                if(size == 0)
                    return Double.NaN;
                else
                {
                    double total = 0;
                    for(Fisher fisher : state.getFishers())
                        total+=  fisher.getMaximumLoad();
                    return total/size;
                }
            }
        },Double.NaN);



    }


    public static void attachGoingOutProbabilityToEveryone(List<Fisher> fishers,
                                                      FishState model)
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
                            probability = probability * (0.8 + 0.4 * random.nextDouble());
                            probability = Math.min(Math.max(0, probability), 1);
                            return new FixedProbabilityDepartingStrategy(probability);
                        }
                    },
                    (fisher1, change, model1) -> fisher1.setDepartingStrategy(change),
                    fisher1 -> ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()),
                    new CashFlowObjective(60),
                    .2, .6
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
