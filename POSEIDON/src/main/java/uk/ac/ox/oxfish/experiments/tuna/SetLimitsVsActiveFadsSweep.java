/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.SetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;
import uk.ac.ox.oxfish.model.scenario.StandardIattcRegulationsFactory;
import uk.ac.ox.oxfish.model.scenario.Subfolder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.time.Month.*;
import static java.util.stream.IntStream.rangeClosed;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;

public class SetLimitsVsActiveFadsSweep {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");

    private static final Path scenarioPath =
        basePath.resolve(Paths.get("runs", "gatherers_test", "tuna_calibrated.yaml"));

    private static final Path outputPath =
        basePath.resolve(Paths.get("runs", "set_limits_vs_closures_sweeps_5_rpp"));

    private static final int NUM_RUNS_PER_POLICY = 25;
    private static final int NUM_YEARS_TO_RUN = 5;
    private static final int POLICY_KICK_IN_YEAR = 3;

    public static void main(final String[] args) {
        new Runner<>(EpoBiomassScenario.class, scenarioPath, outputPath)
            .setPolicies(new SetLimitsVsActiveFadsSweep().makePolicies())
            .requestYearlyData()
            .run(NUM_YEARS_TO_RUN, NUM_RUNS_PER_POLICY);
    }

    private ImmutableList<Policy<? super EpoBiomassScenario>> makePolicies() {

        final AlgorithmFactory<? extends ActionSpecificRegulation> currentFadLimits =
            new ActiveFadLimitsFactory();

        final Map<Integer, SetLimitsFactory> setLimitsFactories =
            rangeClosed(1, 5).boxed().collect(toImmutableMap(
                i -> i * 25,
                i -> new SetLimitsFactory(i * 25)
            ));

        final Map<Integer, Function<EpoBiomassScenario, AlgorithmFactory<? extends Regulation>>> closureFactories =
            rangeClosed(0, 4).boxed().collect(toImmutableMap(
                i -> i * 14,
                i -> scenario ->
                    new CompositeMultipleRegulationsFactory(
                        ImmutableList.of(
                            new ProtectedAreasFromFolderFactory(
                                new Subfolder(scenario.getInputFolder(), "regions"),
                                "region_tags.csv"
                            ),
                            new MultipleRegulationsFactory(ImmutableMap.of(
                                StandardIattcRegulationsFactory.EL_CORRALITO_REG, TAG_FOR_ALL,
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(JULY, 29) - (i * 14),
                                    EpoBiomassScenario.dayOfYear(OCTOBER, 8),
                                    new NoFishingFactory()
                                ), "closure A",
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(NOVEMBER, 9),
                                    EpoBiomassScenario.dayOfYear(JANUARY, 19) + (i * 14),
                                    new NoFishingFactory()
                                ), "closure B"
                            ))
                        )
                    )
            ));

        final ImmutableList.Builder<Policy<? super EpoBiomassScenario>> builder = ImmutableList.builder();

        setLimitsFactories.forEach((i, setLimitsFactory) ->
            closureFactories.forEach((j, closureFactory) ->
                builder.add(makePolicy(
                    i + "," + j,
                    ImmutableList.of(currentFadLimits, setLimitsFactory),
                    closureFactory
                ))
            )
        );

        return builder.build();
    }

    private Policy<EpoBiomassScenario> makePolicy(
        String policyName,
        Collection<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulationFactories,
        Function<EpoBiomassScenario, AlgorithmFactory<? extends Regulation>> makeGeneralRegulationFactory
    ) {
        Consumer<EpoBiomassScenario> scenarioConsumer = scenario -> {
            final Optional<AlgorithmFactory<? extends Regulation>> generalRegulationFactory =
                Optional.ofNullable(makeGeneralRegulationFactory).map(factory -> factory.apply(scenario));
            Steppable setRegulations = simState -> {
                final FishState fishState = (FishState) simState;
                System.out.println("Setting regulations to " + policyName + " for all fishers at day " + simState.schedule
                    .getSteps());
                fishState.getFishers().forEach(fisher -> {
                    generalRegulationFactory.map(factory -> factory.apply(fishState)).ifPresent(fisher::setRegulation);
                    ((PurseSeineGear) fisher.getGear()).getFadManager().setActionSpecificRegulations(
                        actionSpecificRegulationFactories.stream().map(factory -> factory.apply(fishState))
                    );
                });
            };
            scenario.getAdditionalStartables().add(__ -> fishState ->
                fishState.scheduleOnceAtTheBeginningOfYear(setRegulations, StepOrder.AFTER_DATA, POLICY_KICK_IN_YEAR)
            );
        };
        return new Policy<>(policyName, policyName, scenarioConsumer);
    }

}
