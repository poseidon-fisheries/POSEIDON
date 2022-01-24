/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility;

import static org.apache.logging.log4j.core.config.AppenderRef.createAppenderRef;

import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.CsvParameterLayout;

/**
 * Adds a log4j CSV logger. See {@link CsvLogger#addCsvLogger(Level, String, String)} for details.
 */
public class CsvLogger {

    /**
     * Adds a log4j CSV logger. The output will be written to {@code "temp/" + loggerName + ".csv"}.
     * Client code can access the logger with {@code LogManager.getLogger("biomass_events");} and
     * then write to it using, e.g.,:
     *
     * <p>{@code logger.debug(() -> new ObjectArrayMessage(value1, value2, value3)}.
     *
     * <p>It's not strictly necessary to use the lambda syntax, but avoids constructing the
     * message if the logger has not been initialized.
     *
     * @param level      The level at which to start logging
     * @param loggerName The name of the logger, from which the output file name will be derived.
     * @param header     The header of the CSV file.
     */
    public static void addCsvLogger(
        final Level level,
        final String loggerName,
        final String header
    ) {
        // Log4j makes it slightly annoying to configure stuff from Java code
        // as opposed to an XML file, but having all this here conveniently
        // allows us to quickly fire up a custom logger for testing or debugging.

        final LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        final Configuration config = loggerContext.getConfiguration();

        final Layout<String> csvParameterLayout = new CsvParameterLayout(
            config,
            Charset.defaultCharset(),
            CSVFormat.DEFAULT,
            header + "\n",
            null
        );

        final Appender appender = FileAppender.newBuilder()
            .setConfiguration(config)
            .withAppend(false)
            .setName(loggerName + "_appender")
            .setLayout(csvParameterLayout)
            .withFileName("temp/" + loggerName + ".csv")
            .build();
        appender.start();
        config.addAppender(appender);

        final LoggerConfig loggerConfig = LoggerConfig
            .createLogger(
                false,
                level,
                loggerName,
                "true",
                new AppenderRef[] {createAppenderRef(loggerName, null, null)},
                null,
                config,
                null
            );

        loggerConfig.addAppender(appender, null, null);
        config.addLogger(loggerName, loggerConfig);
        loggerContext.updateLoggers();
    }


}
