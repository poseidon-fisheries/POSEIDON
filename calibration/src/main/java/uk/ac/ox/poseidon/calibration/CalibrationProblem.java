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

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableLongArray;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Problem;
import io.jenetics.util.DoubleRange;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationStartOptions;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Accessors(fluent = true)
public class CalibrationProblem
    implements Problem<SequencedMap<String, Double>, DoubleGene, Double> {

    protected final @NonNull Scenario scenario;
    private final @NonNull TemporalAmount duration;
    private final @NonNull ImmutableMap<String, ? extends Factory<? super SimulationScope, ?>>
        extraComponents;
    private final @NonNull ImmutableMap<String, DoubleRange> parameterRanges;
    private final @NonNull ToDoubleFunction<Simulation> evaluator;
    private final @NonNull ImmutableLongArray seeds;

    @Getter(lazy = true)
    private final Codec<SequencedMap<String, Double>, DoubleGene> codec =
        Codec.of(
            Genotype.of(DoubleChromosome.of(0.0, 1.0, this.parameterRanges.size())),
            genotype -> {
                final var entries = this.parameterRanges.entrySet().stream().toList();
                final var chromosome = genotype.chromosome();
                return IntStream
                    .range(0, this.parameterRanges.size())
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
                        (_, b) -> b,
                        LinkedHashMap::new
                    ));
            }
        );

    public Function<SequencedMap<String, Double>, Double> fitness() {
        return this::evaluate;
    }

    private double evaluate(final SequencedMap<String, Double> parameters) {
        return seeds
            .stream()
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
                    .propertyOverrides(parameters)
                    .extraComponents(extraComponents)
                    .build()
            );
        try {
            final TemporalSchedule temporalSchedule = simulation.getTemporalSchedule();
            temporalSchedule.stepFor(simulation, duration);
            return evaluator.applyAsDouble(simulation);
        } finally {
            simulation.finish();
        }
    }

}
