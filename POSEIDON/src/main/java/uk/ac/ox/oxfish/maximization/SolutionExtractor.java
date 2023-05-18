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
