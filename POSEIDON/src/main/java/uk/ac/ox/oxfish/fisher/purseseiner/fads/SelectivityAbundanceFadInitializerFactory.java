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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.environment.EnvironmentalPenalty;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.environment.EnvironmentalPenaltyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceAggregatingFadInitializer;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class SelectivityAbundanceFadInitializerFactory
    extends AbundanceFadInitializerFactory
    implements AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private Map<String, EnvironmentalPenaltyFactory> environmentalPenaltyFactories;

    public SelectivityAbundanceFadInitializerFactory() {
        super();
    }

    public SelectivityAbundanceFadInitializerFactory(
        final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter daysInWaterBeforeAttraction,
        final Map<String, DoubleParameter> catchabilities,
        final Map<String, DoubleParameter> fishReleaseProbabilities,
        final Map<String, EnvironmentalPenaltyFactory> environmentalPenaltyFactories
    ) {
        super(
            carryingCapacitySupplier,
            catchabilities,
            fishReleaseProbabilities,
            daysInWaterBeforeAttraction,
            abundanceFiltersFactory
        );
        this.environmentalPenaltyFactories = environmentalPenaltyFactories;
    }

    protected FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> makeFadInitializer(
        final FishState fishState
    ) {

        final GlobalBiology globalBiology = fishState.getBiology();

        final MersenneTwisterFast rng = fishState.getRandom();

        final double[] catchabilityArray =
            globalBiology.getSpecies().stream()
                .mapToDouble(species ->
                    getCatchabilities()
                        .getOrDefault(species.getName(), new FixedDoubleParameter(0))
                        .applyAsDouble(rng)
                )
                .toArray();

        final BiFunction<? super LocalDate, ? super Fad, double[]> catchabilitySupplier;
        if (environmentalPenaltyFactories.isEmpty()) {
            catchabilitySupplier = (date, fad) -> catchabilityArray;
        } else {
            final List<EnvironmentalPenalty> environmentalPenalties =
                environmentalPenaltyFactories
                    .values()
                    .stream()
                    .map(factory -> factory.apply(fishState))
                    .collect(Collectors.toList());
            catchabilitySupplier = (date, fad) -> {
                final double penalty = environmentalPenalties
                    .stream()
                    .mapToDouble(environmentalPenalty ->
                        environmentalPenalty.applyAsDouble(date, fad.getLocation())
                    )
                    .reduce(1, (v1, v2) -> v1 * v2);
                return (penalty <= 0 || !Double.isFinite(penalty))
                    ? new double[globalBiology.getSize()]
                    : Arrays.stream(catchabilityArray).map(c -> c * penalty).toArray();
            };
        }

        return new AbundanceAggregatingFadInitializer(
            globalBiology,
            new CatchabilitySelectivityFishAttractor(
                catchabilitySupplier,
                (int) getDaysInWaterBeforeAttraction().applyAsDouble(rng),
                fishState,
                getAbundanceFilters().apply(fishState).get(FadSetAction.class)
            ),
            fishState::getStep,
            getCarryingCapacitySupplier().apply(fishState),
            processSpeciesNameToDoubleParameterMap(
                getFishReleaseProbabilities(),
                globalBiology,
                rng
            )
        );
    }

    public Map<String, EnvironmentalPenaltyFactory> getEnvironmentalPenaltyFactories() {
        return environmentalPenaltyFactories;
    }

    public void setEnvironmentalPenaltyFactories(final Map<String, EnvironmentalPenaltyFactory> environmentalPenaltyFactories) {
        this.environmentalPenaltyFactories = environmentalPenaltyFactories;
    }
}
