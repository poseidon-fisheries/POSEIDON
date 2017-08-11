package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;

import java.util.Iterator;

/**
 * if the fisher made losses on  average in the last x consecutive years, he's out
 */
public class ExitDepartingDecorator implements DepartingStrategy {


    private final DepartingStrategy decorated;

    private boolean hasQuit =false;

    private final int consecutiveYearsNegative;

    private Stoppable stoppable;


    public ExitDepartingDecorator(DepartingStrategy decorated, int consecutiveYearsNegative)
    {
        this.decorated = decorated;
        this.consecutiveYearsNegative = consecutiveYearsNegative;
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
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        if(hasQuit)
            return false;
        else
            return decorated.shouldFisherLeavePort(fisher, model, random);
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        //shedule yourself to check for profits every year
        if(stoppable != null)
            throw  new RuntimeException("Already started!");

        Steppable steppable = new Steppable() {
            @Override
            public void step(SimState simState) {
                checkIfQuit(fisher);
            }
        };
        this.stoppable = model.scheduleEveryYear(steppable, StepOrder.DAWN);
        decorated.start(model,fisher);

    }

    @Override
    public void turnOff(Fisher fisher) {

        Preconditions.checkArgument(stoppable != null, "Can't turn off ");
        this.stoppable.stop();
        decorated.turnOff(fisher);
    }




    public void checkIfQuit(Fisher fisher){
        if(!hasQuit) {
            DataColumn profitData = fisher.getYearlyData().getColumn(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
            if(profitData.size() >= consecutiveYearsNegative) {
                Iterator<Double> profitsIterator = profitData.descendingIterator();
                double sum = 0;
                for(int i=0; i<consecutiveYearsNegative; i++) {
                    sum+=profitsIterator.next();
                }
                //you are here, all your profits were negative!
                if(sum<0)
                    hasQuit = true;
            }
        }
    }

    public boolean isHasQuit() {
        return hasQuit;
    }
}
