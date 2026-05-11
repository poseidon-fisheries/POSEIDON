/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.calibration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.DoubleRange;
import lombok.Getter;
import lombok.experimental.Accessors;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationStartOptions;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Getter
@Accessors(fluent = true)
public abstract class CalibrationProblem
    implements Problem<SequencedMap<String, Double>, DoubleGene, Double> {

    private final Scenario scenario;
    private final TemporalAmount temporalAmount;
    private final SequencedMap<String, DoubleRange> ranges;
    private final long[] seeds;

    private final Codec<SequencedMap<String, Double>, DoubleGene> codec;

    public CalibrationProblem(
        final Scenario scenario,
        final TemporalAmount temporalAmount,
        final List<ParameterRange> ranges
    ) {
        this(scenario, temporalAmount, ranges, new long[]{0});
    }

    public CalibrationProblem(
        final Scenario scenario,
        final TemporalAmount temporalAmount,
        final List<ParameterRange> ranges,
        final long... seeds
    ) {
        this(
            scenario,
            temporalAmount,
            toDoubleRanges(ranges),
            seeds
        );
    }

    public CalibrationProblem(
        final Scenario scenario,
        final TemporalAmount temporalAmount,
        final SequencedMap<String, DoubleRange> ranges
    ) {
        this(scenario, temporalAmount, ranges, new long[]{0});
    }

    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "Calibration problems intentionally wrap the supplied scenario and temporal amount."
    )
    public CalibrationProblem(
        final Scenario scenario,
        final TemporalAmount temporalAmount,
        final SequencedMap<String, DoubleRange> ranges,
        final long... seeds
    ) {
        this.scenario = scenario;
        this.temporalAmount = temporalAmount;
        this.ranges = Collections.unmodifiableSequencedMap(new LinkedHashMap<>(ranges));
        this.seeds = seeds.length == 0 ? new long[]{0} : Arrays.copyOf(seeds, seeds.length);
        this.codec =
            Codec.of(
                Genotype.of(DoubleChromosome.of(0.0, 1.0, this.ranges.size())),
                genotype -> {
                    final var entries = this.ranges.sequencedEntrySet().stream().toList();
                    final var chromosome = genotype.chromosome();
                    return IntStream
                        .range(0, this.ranges.size())
                        .mapToObj(i -> {
                            final var entry = entries.get(i);
                            final DoubleRange range = entry.getValue();
                            final double allele = chromosome.get(i).allele();
                            return java.util.Map.entry(
                                entry.getKey(),
                                range.min() + allele * (range.max() - range.min())
                            );
                        })
                        .collect(toMap(
                            java.util.Map.Entry::getKey,
                            java.util.Map.Entry::getValue,
                            (a, b) -> b,
                            LinkedHashMap::new
                        ));
                }
            );
    }

    public Function<SequencedMap<String, Double>, Double> fitness() {
        return this::evaluate;
    }

    private double evaluate(final SequencedMap<String, Double> parameters) {
        return Arrays
            .stream(seeds)
            .mapToDouble(seed -> evaluate(parameters, seed))
            .average()
            .orElseThrow();
    }

    private double evaluate(
        final SequencedMap<String, Double> parameters,
        final long seed
    ) {
        final Simulation simulation =
            scenario.startNewSimulation(
                SimulationStartOptions
                    .builder()
                    .seed(seed)
                    .propertyOverrides(new LinkedHashMap<String, Object>(parameters))
                    .build()
            );
        try {
            final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
            temporalSchedule.stepFor(simulation, temporalAmount);
            return evaluate(simulation);
        } finally {
            simulation.finish();
        }
    }

    protected abstract double evaluate(Simulation simulation);

    private static SequencedMap<String, DoubleRange> toDoubleRanges(
        final List<ParameterRange> ranges
    ) {
        return ranges
            .stream()
            .collect(
                LinkedHashMap::new,
                (map, range) -> map.put(
                    range.propertyName(),
                    new DoubleRange(range.min(), range.max())
                ),
                SequencedMap::putAll
            );
    }
}
