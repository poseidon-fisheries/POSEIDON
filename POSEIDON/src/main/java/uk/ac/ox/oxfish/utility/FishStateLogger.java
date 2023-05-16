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

package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.model.FishState;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Just a logger formatter that prints out as date the model date rather than the clock time
 * Created by carrknight on 6/30/15.
 */
public class FishStateLogger extends Logger {

    private final FishState model;
    private final Path path;
    private final Logger logger;
    private FileWriter writer;

    public FishStateLogger(final FishState model, final Path pathToFile) throws IOException {
        super("uk.ac.ox.oxfish.utility.FishStateLogger", null);
        this.logger = Logger.getLogger(getName());
        this.model = model;
        this.path = pathToFile;
    }

    @Override
    public void log(final LogRecord logRecord) {

        final StringBuilder builder = new StringBuilder(256);
        builder.append(model.timeString());
        builder.append(',');
        builder.append(logRecord.getLevel());
        builder.append(',');
        builder.append(logRecord.getMessage());

        System.out.println(builder);
        try {
            if (writer == null)
                writer = new FileWriter(path.toFile());
            writer.write(builder.toString());
            writer.write('\n');
            writer.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


}
