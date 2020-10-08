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

import static java.time.Month.FEBRUARY;
import static java.time.Month.JULY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static uk.ac.ox.oxfish.model.regs.MultipleRegulations.TAG_FOR_ALL;

public class NinePolicies {

    private static final Path basePath =
        Paths.get(System.getProperty("user.home"), "workspace", "tuna", "np");

    private static final Path scenarioPath =
        basePath.resolve(Paths.get("calibrations", "2020-08-10B", "tuna_calibrated.yaml"));

    private static final Path outputPath =
        basePath.resolve(Paths.get("runs", "nine_policies_2020-09-15"));

    private static final int NUM_RUNS_PER_POLICY = 20;
    private static final int NUM_YEARS_TO_RUN = 3;
    private static final int POLICY_KICK_IN_YEAR = 2;

    public static void main(final String[] args) {
        new Runner<>(TunaScenario.class, scenarioPath, outputPath)
            .setPolicies(new NinePolicies().makePolicies())
            .requestYearlyData()
            .run(NUM_YEARS_TO_RUN, NUM_RUNS_PER_POLICY);
    }

    private ImmutableList<Policy<TunaScenario>> makePolicies() {

        final AlgorithmFactory<? extends ActionSpecificRegulation> currentFadLimits =
            new ActiveFadLimitsFactory();

        final AlgorithmFactory<? extends ActionSpecificRegulation> strictFadLimits =
            new ActiveFadLimitsFactory(0, 0, 75, 115);

        return ImmutableList.of(
            makePolicy(
                "25-set limit / Strict FAD limits",
                ImmutableList.of(strictFadLimits, new SetLimitsFactory(25)),
                null
            ),
            makePolicy(
                "25-set limit / Current FAD limit",
                ImmutableList.of(currentFadLimits, new SetLimitsFactory(25)),
                null
            ),
            makePolicy(
                "50-set limit / Strict FAD limits",
                ImmutableList.of(strictFadLimits, new SetLimitsFactory(50)),
                null
            ),
            makePolicy(
                "50-set limit / Current FAD limit",
                ImmutableList.of(currentFadLimits, new SetLimitsFactory(50)),
                null
            ),
            makePolicy(
                "Strict FAD limits / 75-set limit",
                ImmutableList.of(strictFadLimits, new SetLimitsFactory(75)),
                null
            ),
            makePolicy(
                "Current FAD limit / 75-set limit",
                ImmutableList.of(currentFadLimits, new SetLimitsFactory(75)),
                null
            ),
            makePolicy(
                "86-day closures",
                ImmutableList.of(currentFadLimits),
                scenario -> new MultipleRegulationsFactory(ImmutableMap.of(
                    scenario.galapagosEezReg, TAG_FOR_ALL,
                    scenario.elCorralitoReg, TAG_FOR_ALL,
                    new TemporaryRegulationFactory(
                        scenario.dayOfYear(JULY, 15),
                        scenario.dayOfYear(OCTOBER, 8),
                        new NoFishingFactory()
                    ), "closure A",
                    new TemporaryRegulationFactory(
                        scenario.dayOfYear(NOVEMBER, 9),
                        scenario.dayOfYear(FEBRUARY, 2),
                        new NoFishingFactory()
                    ), "closure B"
                ))
            ),
            makePolicy(
                "100-day closures",
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
            ),
            makePolicy(
                "Business as usual",
                ImmutableList.of(currentFadLimits),
                null
            )
        );
    }

    private Policy<TunaScenario> makePolicy(
        String policyName,
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
        return new Policy<>(policyName, policyName, scenarioConsumer);
    }

}
