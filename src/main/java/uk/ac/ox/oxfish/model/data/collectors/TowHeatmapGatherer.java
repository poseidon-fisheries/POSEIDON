package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Utility to keep track of the tows as a heatmap
 * Created by carrknight on 11/15/16.
 */
public class TowHeatmapGatherer implements Steppable, Startable{


    /**
     * the tow heatmap
     */
    private double[][] towHeatmap;

    /**
     * which year should we start collecting data?
     */
    private final int startYear;
    private Stoppable receipt;


    public TowHeatmapGatherer(int startYear) {
        this.startYear = startYear;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param state the model
     */
    @Override
    public void start(FishState state) {
        towHeatmap = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        receipt = state.scheduleEveryDay(this, StepOrder.DAILY_DATA_GATHERING);
    }

    @Override
    public void step(SimState simState) {

        FishState model = (FishState) simState;
        if(model.getYear()>=startYear) {
            IntGrid2D trawls = model.getMap().getDailyTrawlsMap();

            //remember to flip it
            for (int x = 0; x < model.getMap().getWidth(); x++)
                for (int y = 0; y < model.getMap().getHeight(); y++)
                    towHeatmap[x][model.getMap().getHeight() - y - 1] += trawls.get(x, y);
        }

    }


    /**
     * Getter for property 'towHeatmap'.
     *
     * @return Value for property 'towHeatmap'.
     */
    public double[][] getTowHeatmap() {
        return towHeatmap;
    }

    /**
     * Getter for property 'startYear'.
     *
     * @return Value for property 'startYear'.
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(receipt!=null)
            receipt.stop();
    }
}
