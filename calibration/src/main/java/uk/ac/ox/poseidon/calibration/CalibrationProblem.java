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
import java.util.LinkedHashMap;
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

    private final Codec<SequencedMap<String, Double>, DoubleGene> codec;

    public CalibrationProblem(
        final Scenario scenario,
        final TemporalAmount temporalAmount,
        final SequencedMap<String, DoubleRange> ranges
    ) {
        this.scenario = scenario;
        this.temporalAmount = temporalAmount;
        this.ranges = ranges;
        this.codec =
            Codec.of(
                Genotype.of(DoubleChromosome.of(0.0, 1.0, ranges.size())),
                genotype -> {
                    final var entries = ranges.sequencedEntrySet().stream().toList();
                    final var chromosome = genotype.chromosome();
                    return IntStream
                        .range(0, ranges.size())
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
        final Simulation simulation =
            scenario.startNewSimulation(
                SimulationStartOptions
                    .builder()
                    .propertyOverrides(new LinkedHashMap<String, Object>(parameters))
                    .build()
            );
        final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
        temporalSchedule.stepFor(simulation, temporalAmount);
        simulation.finish();
        return evaluate(simulation);
    }

    protected abstract double evaluate(Simulation simulation);
}
