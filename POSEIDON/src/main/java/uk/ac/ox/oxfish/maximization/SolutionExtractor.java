/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparingDouble;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Extracts solutions from a calibration_log.md file
 */
public class SolutionExtractor {

    private final Path logFilePath;

    public SolutionExtractor(final Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    public Entry<double[], Double> bestSolution() {
        return allSolutions()
            .stream()
            .min(comparingDouble(Entry::getValue))
            .orElseThrow(() -> new IllegalStateException("No solution found in " + logFilePath));
    }

    public List<Entry<double[], Double>> allSolutions() {
        try (final Stream<String> lines = Files.lines(logFilePath)) {
            final Pattern p = Pattern.compile("^\\| \\d+ \\| ([\\d.]+) \\|.*\\{(.*)}.*$");
            return lines.map(p::matcher)
                .filter(Matcher::matches)
                .map(m -> entry(
                    Stream.of(m.group(2).split(",")).mapToDouble(Double::parseDouble).toArray(),
                    Double.parseDouble(m.group(1))
                ))
                .collect(toImmutableList());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


}
