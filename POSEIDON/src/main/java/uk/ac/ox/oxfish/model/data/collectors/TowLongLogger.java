/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.OutputPlugin;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;

import java.util.Map;

/**
 * listens to all trips and writes down a long data frame collecting where the tows occurred
 */
public class TowLongLogger implements AdditionalStartable, OutputPlugin, TripListener {


    final private static String HEADER = "fisherid,effort,x_cell,y_cell," +
        "date_trip_end_year,date_trip_end_day,tags";
    private static final long serialVersionUID = 8801745951745956045L;
    private final StringBuilder log = new StringBuilder().append(HEADER).append("\n");
    private String fileName = "tow_log.csv";
    private FishState model;


    public TowLongLogger() {
    }


    public TowLongLogger(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {
        final int year = (int) (record.getTripDay() / 365d);


        for (final Map.Entry<SeaTile, FishingRecord> fishingRecord : record.getFishingRecords()) {
            log.append(fisher.getID()).append(",")
                .append(fishingRecord.getValue().getHoursSpentFishing()).append(",")
                .append(fishingRecord.getKey().getGridX()).append(",")
                .append(fishingRecord.getKey().getGridY()).append(",")
                .append(year).append(",")
                .append(record.getTripDay()).append(",")
                .append(String.join(";", fisher.getTagsList())).append("\n");
        }

    }


    @Override
    public void reactToEndOfSimulation(final FishState state) {
        //nothing
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        Preconditions.checkState(this.model == null, "Already started!");

        this.model = model;
        for (final Fisher fisher : model.getFishers()) {
            fisher.addTripListener(this);
        }

        for (final Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
            fisherFactory.getValue().getAdditionalSetups().add(
                fisher -> {
                    if (TowLongLogger.this.model != null) //if i am still active
                        fisher.addTripListener(TowLongLogger.this);
                }
            );
        }

        this.model.getOutputPlugins().add(this);
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        Preconditions.checkState(model != null, "Not started!");
        for (final Fisher fisher : model.getFishers()) {
            fisher.removeTripListener(this);
        }

        model = null;

    }


    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for property 'fileName'.
     *
     * @param fileName Value to set for property 'fileName'.
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String composeFileContents() {
        return log.toString();
    }


}
