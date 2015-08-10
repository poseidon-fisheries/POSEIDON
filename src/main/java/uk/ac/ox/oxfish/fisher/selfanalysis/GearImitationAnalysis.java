package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityThrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.maximization.Adaptation;

import java.util.List;
import java.util.function.Function;

/**
 * Run every period, check how are you doing, then check a friend. If he is doing better than you have a small probability
 * of copying their gear (without paying the price)
 * Created by carrknight on 8/4/15.
 */
public class GearImitationAnalysis implements Startable
{

    /**
     * how often do we do this?
     */
    private final int period;

    /**
     * the algorithm doing the exploration-imitation thing
     */
    private final Adaptation<Gear> algorithm;


    private final Fisher fisher;

    private Stoppable stoppable;


    public GearImitationAnalysis(
            int period, double probabilityRandomizing, double probabilityImitating,
            List<Gear> gearAvailable, Fisher fisher, ObjectiveFunction<Fisher> objective) {
        this.period = period;
        algorithm = new Adaptation<>(fisher1 -> true,
                                                           new DiscreteRandomAlgorithm<Gear>(gearAvailable),
                                                           (fisher1, change,model) -> fisher1.setGear(change.cloneGear()),
                                                           Fisher::getGear,
                                                           objective,probabilityRandomizing,probabilityImitating);



        this.fisher = fisher;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    public void start(FishState model) {
        stoppable = model.scheduleEveryXDay(simState -> analyze(fisher.grabRandomizer()), StepOrder.AFTER_DATA, period);
    }

    /**
     * tell the startable to turnoff,
     */
    public void turnOff() {
        stoppable.stop();

    }



    public void analyze(MersenneTwisterFast random)
    {

       algorithm.adapt(fisher,random);

    }


    /**
     * utility function to add the imitation analysis to every fisher in the group
     * @param fishers
     * @param model
     * @param randomGear
     */
    public static void attachGearAnalysisToEachFisher(
            List<Fisher> fishers, FishState model, final List<Gear> randomGear)
    {
        //add analysis
        for(Fisher fisher : fishers)
        {
            GearImitationAnalysis analysis = new GearImitationAnalysis(60, 0, .25, randomGear,
                                                                       fisher, new CashFlowObjective(60));

            model.registerStartable(analysis);
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
                double size =state.getFishers().size();
                if(size == 0)
                    return Double.NaN;
                else
                {
                    double total = 0;
                    for(Fisher fisher : state.getFishers())
                        total+= ((RandomCatchabilityThrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0];
                    return total/size;
                }
            }
        },Double.NaN);
    }
}
