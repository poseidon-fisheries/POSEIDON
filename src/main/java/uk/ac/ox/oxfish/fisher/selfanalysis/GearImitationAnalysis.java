package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.List;

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
     * How do we compare ourselves and others
     */
    private ObjectiveFunction objective;

    /**
     * probability of trying a random new gear
     */
    private final double probabilityRandomizing;

    /**
     * probability to copy a friend doing better
     */
    private final double probabilityImitating;

    /**
     * this is the list of gear that you can get when you randomize
     */
    private final List<Gear> gearAvailable;

    private final Fisher fisher;

    private Stoppable stoppable;


    public GearImitationAnalysis(
            int period, double probabilityRandomizing, double probabilityImitating,
            List<Gear> gearAvailable, Fisher fisher, ObjectiveFunction objective) {
        this.period = period;
        this.probabilityRandomizing = probabilityRandomizing;
        this.probabilityImitating = probabilityImitating;
        this.gearAvailable = gearAvailable;
        this.fisher = fisher;
        this.objective = objective;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    public void start(FishState model) {
        stoppable = model.scheduleEveryXDay(simState -> analyze(fisher,fisher.grabRandomizer()), StepOrder.AFTER_DATA, period);
    }

    /**
     * tell the startable to turnoff,
     */
    public void turnOff() {
        stoppable.stop();

    }



    public void analyze(Fisher analyzed, MersenneTwisterFast random)
    {

        //if randomizing, then randomize
        if(probabilityRandomizing > 0 && random.nextBoolean(probabilityRandomizing)) {
            analyzed.setGear(gearAvailable.get(random.nextInt(gearAvailable.size())));
            System.out.println("randomize!");

        }
        else if(probabilityImitating > 0 && random.nextBoolean(probabilityImitating))
        {
            //compute your fitness
            double fitness = objective.computeCurrentFitness(analyzed);
            //grab a friend
            Object[] friends = analyzed.getDirectedFriends().toArray();
            //if you have no friend, return
            if(friends.length == 0)
                return;

            Fisher friend = (Fisher) friends[random.nextInt(friends.length)];
            //compute your friend fitness
            double friendFitness = objective.computeCurrentFitness(friend);

            //if it's better, grab his gear
            if(Double.isFinite(fitness) && Double.isFinite(friendFitness) && friendFitness > fitness) {
                analyzed.setGear(friend.getGear().cloneGear());
            }
        }

    }
}
