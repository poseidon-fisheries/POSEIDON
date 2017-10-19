/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.gui;

import com.google.common.base.Preconditions;
import org.jfree.data.xy.XYSeries;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.media.chart.TimeSeriesChartGenerator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Takes one or more data-columns and chart them
 * Created by carrknight on 6/9/15.
 */
public class DataCharter {


    private final Function<FishState,Double> xColumn;

    private final Map<DataColumn,XYSeries> seriesMap;

    private final TimeSeriesChartGenerator chart;


    private Stoppable stoppable;


    public DataCharter(IntervalPolicy policy, DataColumn column) {
        chart = new TimeSeriesChartGenerator();
        chart.setTitle(column.getName());
        chart.setYAxisLabel(column.getName());

        switch (policy)
        {
            case EVERY_YEAR:
                xColumn = state -> (double)state.getYear();
                chart.setXAxisLabel("Year");
                break;
            case EVERY_DAY:
                xColumn = state -> state.getYear() *365d + state.getDayOfTheYear();
                chart.setXAxisLabel("Day");
                break;
            default:
            case EVERY_STEP:
                xColumn = state -> state.schedule.getTime();
                chart.setXAxisLabel("Step");
                break;
        }

        seriesMap = new HashMap<>();
        final XYSeries series = new XYSeries(column.getName());
        chart.addSeries(series,null);
        seriesMap.put(column, series);
    }


    public void start(GUIState gui)
    {

        Preconditions.checkState(stoppable == null, "Already started!");

        assert SwingUtilities.isEventDispatchThread();
        //get each series with its data
        for(Map.Entry<DataColumn,XYSeries> entry :seriesMap.entrySet())
        {
            //if the data-column already has values, populate it on the fly
            int numberOfObservations = entry.getKey().size();
            if(numberOfObservations>0)
            {
                //what's the current date?
                double currentX = xColumn.apply((FishState) gui.state);
                for(int i=0; i<numberOfObservations ; i ++)
                {
                    //add them assuming fixed distance
                    entry.getValue().add(currentX-numberOfObservations+i,entry.getKey().get(i));
                }
                assert entry.getValue().getItemCount() == entry.getKey().size(); //should be of the same size now!
            }

        }

        //schedule for update
        stoppable = gui.scheduleRepeatingImmediatelyAfter(new Steppable() {
            @Override
            public void step(SimState simState) {

                //for every column
                for(Map.Entry<DataColumn,XYSeries> entry :seriesMap.entrySet())
                {
                    if(entry.getKey().size() > entry.getValue().getItemCount())
                    {
                        //should be different only by one at most
//                        assert entry.getValue().getItemCount() == entry.getKey().size() + 1;
                        //not true in fast real time situations
                        entry.getValue().add(xColumn.apply((FishState) simState),entry.getKey().getLatest());

                    }

                    assert entry.getValue().getItemCount() == entry.getKey().size();


                }

                chart.updateChartLater(simState.schedule.getSteps());
            }
        });

        //make visible
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame frame = chart.createFrame();
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
                frame.pack();
                //register with controller
                gui.controller.registerFrame(frame);
                //turnOff on dispose
                frame.addWindowListener(new WindowListener() {
                    @Override
                    public void windowOpened(WindowEvent e) {

                    }

                    @Override
                    public void windowClosing(WindowEvent e) {

                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        stoppable.stop();
                    }

                    @Override
                    public void windowIconified(WindowEvent e) {

                    }

                    @Override
                    public void windowDeiconified(WindowEvent e) {

                    }

                    @Override
                    public void windowActivated(WindowEvent e) {

                    }

                    @Override
                    public void windowDeactivated(WindowEvent e) {

                    }
                });
            }
        });

    }
}
