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
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.MultipleRegulationsFactory;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.regs.factory.TemporaryRegulationFactory;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.SetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JULY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.util.stream.Stream.concat;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;
import static uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory.iattcLimits;

public class SetLimitsVsActiveFadsSweep {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");

    private static final Path scenarioPath =
        basePath.resolve(Paths.get("runs", "gatherers_test", "tuna_calibrated.yaml"));

    private static final Path outputPath =
        basePath.resolve(Paths.get("runs", "set_limits_vs_closures_sweeps"));

    private static final int NUM_RUNS_PER_POLICY = 2;
    private static final int NUM_YEARS_TO_RUN = 6;
    private static final int POLICY_KICK_IN_YEAR = 1;

    private static final AlgorithmFactory<? extends ActionSpecificRegulation> currentFadLimits =
        new ActiveFadLimitsFactory(iattcLimits);

    public static void main(final String[] args) {
        new SetLimitsVsActiveFadsSweep().makeRunner().run(NUM_YEARS_TO_RUN);
    }

    private Runner<TunaScenario> makeRunner() {

        final ImmutableMap<Optional<AlgorithmFactory<? extends ActionSpecificRegulation>>, String> setLimits =
            concat(
                Stream.of(0, 25, 50, 75).map(Optional::of),
                Stream.of(Optional.<Integer>empty())
            ).collect(toImmutableMap(
                opt -> opt.map(SetLimitsFactory::new),
                opt -> opt.map(limit -> limit + " sets limit").orElse("No set limit")
            ));

        Policy<TunaScenario> p =
            makePolicy(
                "Tuna - Longer seasonal closures",
                "Length of closure periods A and B increased by 40% (100 days instead of 72).",
                ImmutableList.of(currentFadLimits),
                scenario -> new MultipleRegulationsFactory(ImmutableMap.of(
                    scenario.galapagosEezReg, TAG_FOR_ALL,
                    scenario.elCorralitoReg, TAG_FOR_ALL,
                    new TemporaryRegulationFactory(
                        scenario.dayOfYear(JULY, 1),
                        scenario.dayOfYear(OCTOBER, 8),
                        new NoFishingFactory()
                    ), "closure A",
                    new TemporaryRegulationFactory(
                        scenario.dayOfYear(NOVEMBER, 9),
                        scenario.dayOfYear(FEBRUARY, 16),
                        new NoFishingFactory()
                    ), "closure B"
                ))
            );

        ImmutableList.Builder<Policy<TunaScenario>> policies = ImmutableList.builder();
//        fadLimits.forEach((activeFadLimits, fadLimitsName) ->
//            setLimits.forEach((generalSetLimits, setLimitsName) ->
//                policies.add(makePolicy(
//                    concat(Stream.of(activeFadLimits), stream(generalSetLimits)).collect(toImmutableList()),
//                    fadLimitsName + " / " + setLimitsName
//                ))
//            )
//        );

        return new Runner<>(TunaScenario.class, scenarioPath, outputPath)
            .requestYearlyData()
            .setPolicies(policies.build());
    }

    private Policy<TunaScenario> makePolicy(
        String policyName,
        String policyDescription,
        Collection<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulationFactories,
        Function<TunaScenario, AlgorithmFactory<? extends Regulation>> makeGeneralRegulationFactory
    ) {
        Consumer<TunaScenario> scenarioConsumer = scenario -> {
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
            scenario.getPlugins().add(__ -> fishState ->
                fishState.scheduleOnceAtTheBeginningOfYear(setRegulations, StepOrder.AFTER_DATA, POLICY_KICK_IN_YEAR)
            );
        };
        return new Policy<>(policyName, policyDescription, scenarioConsumer);
    }

}
