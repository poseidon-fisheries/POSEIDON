package uk.ac.ox.oxfish.fisher.strategies.destination;

import burlap.datastructures.BoltzmannDistribution;
import com.google.common.base.Preconditions;
import javafx.collections.ObservableList;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Uses a simple SOFTMAX rule to force each agent to change its destination strategy as if evolving.
 *
 * Created by carrknight on 2/28/17.
 */
public class StrategyReplicator implements Startable,Steppable{


    private final List<AlgorithmFactory<? extends DestinationStrategy>> options;

    private final double lastObservedFitnesses[];
    private double temperature = 1;

    private final ObjectiveFunction<Fisher> objectiveFunction;
    private Stoppable stoppable;


    /**
     * individual probability of not replicating
     */
    private final double inertia ;

    public StrategyReplicator(
            List<AlgorithmFactory<? extends DestinationStrategy>> options,
            ObjectiveFunction<Fisher> objectiveFunction, double inertia) {
        this.options = options;
        this.lastObservedFitnesses = new double[options.size()];
        this.objectiveFunction = objectiveFunction;
        this.inertia = inertia;
        Preconditions.checkArgument(options.size() >= 2);
    }


    @Override
    public void step(SimState simState) {
        //update fitnesses (average utility per fisher per strategy)
        FishState model = (FishState) simState;
        updateFitnesses(model);

        //now for each fisher sample a new strategy
        evolve(model);


    }

    private void evolve(FishState model) {
        //softmax selector
        BoltzmannDistribution distribution = new BoltzmannDistribution(lastObservedFitnesses,temperature);
        for(Fisher fisher : (model).getFishers()) {
            //inertia can block you as well as regulations
            if(!model.getRandom().nextBoolean(inertia) && fisher.isAllowedAtSea() && fisher.getHoursAtPort() < 48) {
                int newStrategy = distribution.sample();
                fisher.setDestinationStrategy(
                        new ReplicatorDrivenDestinationStrategy(newStrategy,
                                                                options.get(newStrategy).apply(model)));
            }
        }
    }

    private void updateFitnesses(FishState simState) {
        ObservableList<Fisher> fishers = simState.getFishers();
        DoubleSummaryStatistics statistics[] =
                new DoubleSummaryStatistics[lastObservedFitnesses.length];
        Arrays.setAll(statistics, value -> new DoubleSummaryStatistics());
        //get each fishers' utility
        for(Fisher fisher : fishers)
        {

            int index = ((ReplicatorDrivenDestinationStrategy) fisher.getDestinationStrategy()).getStrategyIndex();
            statistics[index].accept(objectiveFunction.computeCurrentFitness(fisher));
        }
        for(int i=0; i<options.size(); i++)
        {
            lastObservedFitnesses[i] = statistics[i].getAverage();


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

        Preconditions.checkArgument(stoppable==null);
        //schedule yourself
        stoppable = model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, 60);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(stoppable!=null)
            stoppable.stop();

    }

    /**
     * Getter for property 'options'.
     *
     * @return Value for property 'options'.
     */
    public List<AlgorithmFactory<? extends DestinationStrategy>> getOptions() {
        return options;
    }

    /**
     * Getter for property 'lastObservedFitnesses'.
     *
     * @return Value for property 'lastObservedFitnesses'.
     */
    public double[] getLastObservedFitnesses() {
        return lastObservedFitnesses;
    }

    /**
     * Getter for property 'inertia'.
     *
     * @return Value for property 'inertia'.
     */
    public double getInertia() {
        return inertia;
    }
}
