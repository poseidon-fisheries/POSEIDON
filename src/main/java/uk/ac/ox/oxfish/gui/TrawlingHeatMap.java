package uk.ac.ox.oxfish.gui;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.util.gui.SimpleColorMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingAverage;

import java.awt.*;

/**
 * Basically an aggregator of the NauticalMap's daily trawl map that instead of resetting every day
 * slowly dampens itself for more pleasing aestetic
 * Created by carrknight on 10/2/15.
 */
public class TrawlingHeatMap implements Steppable{


    /**
     * the actual map we want to display
     */
    private final ObjectGrid2D smoothedHeatMap;


    private final FastObjectGridPortrayal2D heatMapPortrayal;


    /**
     * the source of the trawling data
     */
    private final IntGrid2D trawlMap;


    private double maximum;

    public TrawlingHeatMap(IntGrid2D trawlMap, FishState model, final int movingAverageSize)
    {

        this.trawlMap = trawlMap;
        maximum = 1;

        smoothedHeatMap = new ObjectGrid2D(trawlMap.getWidth(),trawlMap.getHeight());
        for(int x =0; x<smoothedHeatMap.getWidth(); x++)
        {
            for (int y = 0; y < smoothedHeatMap.getHeight(); y++)
            {
                MovingAverage<Integer> averager = new MovingAverage<>(movingAverageSize);
                averager.addObservation(0); //start it at 0
                smoothedHeatMap.set(x,y, averager);
            }
        }

        heatMapPortrayal = new FastObjectGridPortrayal2D(false){
            @Override
            public double doubleValue(Object obj) {
                double average = ((MovingAverage) obj).getSmoothedObservation();
                if(!Double.isFinite(average))
                    return 0;
                else
                    return average;
            }
        };
        heatMapPortrayal.setField(smoothedHeatMap);
        heatMapPortrayal.setMap(new SimpleColorMap(0,maximum,new Color(0,0,0,0),Color.RED));

        model.scheduleEveryDay(this, StepOrder.DATA_RESET);
    }


    @Override
    public void step(SimState simState)
    {
        //go through all trawls and add that number to the list
        double newMaximum = 0;
        for(int x =0; x<smoothedHeatMap.getWidth(); x++)
        {
            for (int y = 0; y < smoothedHeatMap.getHeight(); y++)
            {
                @SuppressWarnings("unchecked")
                MovingAverage<Integer> averager = (MovingAverage<Integer>) smoothedHeatMap.get(x, y);
                averager.addObservation(trawlMap.get(x,y));
                assert averager.isReady();
                newMaximum = Math.max(newMaximum,averager.getSmoothedObservation());
            }
        }
        //change the color if the current maximum is 15% above the map maximum
        if(.85*newMaximum>maximum)
        {
            maximum = .85 *  newMaximum;
            heatMapPortrayal.setMap(new SimpleColorMap(0,maximum,new Color(0,0,0,0),Color.RED));

        }
    }

    public FastObjectGridPortrayal2D getHeatMapPortrayal() {
        return heatMapPortrayal;
    }
}
