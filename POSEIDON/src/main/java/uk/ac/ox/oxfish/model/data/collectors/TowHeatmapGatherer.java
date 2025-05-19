/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.data.collectors;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * Utility to keep track of the tows as a heatmap
 * Created by carrknight on 11/15/16.
 */
public class TowHeatmapGatherer implements Steppable, Startable {


    private static final long serialVersionUID = 7246978782446924067L;
    /**
     * which year should we start collecting data?
     */
    private final int startYear;
    /**
     * the tow heatmap
     */
    private double[][] towHeatmap;
    private Stoppable receipt;


    public TowHeatmapGatherer(final int startYear) {
        this.startYear = startYear;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param state the model
     */
    @Override
    public void start(final FishState state) {
        towHeatmap = new double[state.getMap().getWidth()][state.getMap().getHeight()];
        receipt = state.scheduleEveryDay(this, StepOrder.DAILY_DATA_GATHERING);
    }

    @Override
    public void step(final SimState simState) {

        final FishState model = (FishState) simState;
        if (model.getYear() >= startYear) {
            final IntGrid2D trawls = model.getMap().getDailyTrawlsMap();

            //remember to flip it
            for (int x = 0; x < model.getMap().getWidth(); x++)
                for (int y = 0; y < model.getMap().getHeight(); y++) {
                    if (model.getMap().getSeaTile(x, y).isLand())
                        towHeatmap[x][model.getMap().getHeight() - y - 1] = Double.NaN;
                    else
                        towHeatmap[x][model.getMap().getHeight() - y - 1] += trawls.get(x, y);
                }
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
        if (receipt != null)
            receipt.stop();
    }
}
