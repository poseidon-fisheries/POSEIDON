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

package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.LinkedList;

/**
 * Collection of logistic log that can output to file.
 * Created by carrknight on 1/10/17.
 */
public class LogisticLogs extends LinkedList<LogisticLog> implements OutputPlugin {


    private static final long serialVersionUID = -4644229479131089976L;
    private String filename = "logistic_long.csv";

    @Override
    public void reactToEndOfSimulation(final FishState state) {
        //ignored
    }

    @Override
    public String getFileName() {
        return filename;
    }

    /**
     * Setter for property 'filename'.
     *
     * @param filename Value to set for property 'filename'.
     */
    public void setFileName(final String filename) {
        this.filename = filename;
    }

    /**
     * create a "long-format" csv file by row-binding all the separate loggers
     *
     * @return
     */
    @Override
    public String composeFileContents() {
        if (isEmpty())
            return "";

        final StringBuilder builder = new StringBuilder();
        builder.append(get(0).getColumnNames()).append("\n");
        for (final LogisticLog log : this)
            builder.append(log.getData().toString());
        return builder.toString();

    }
}
