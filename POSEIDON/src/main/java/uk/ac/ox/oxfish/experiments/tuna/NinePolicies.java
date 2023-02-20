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
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.regs.fads.SetLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;
import uk.ac.ox.oxfish.model.scenario.StandardIattcRegulationsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.function.Function;

import static java.time.Month.*;
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
        new Runner<>(EpoBiomassScenario.class, scenarioPath, outputPath)
            .setPolicies(new NinePolicies().makePolicies())
            .requestYearlyData()
            .run(NUM_YEARS_TO_RUN, NUM_RUNS_PER_POLICY);
    }

    private ImmutableList<Policy<? super EpoBiomassScenario>> makePolicies() {

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
                scenario ->
                    new CompositeMultipleRegulationsFactory(
                        ImmutableList.of(
                            new ProtectedAreasFromFolderFactory(
                                scenario.getInputFolder().path("regions"),
                                "region_tags.csv"
                            ),
                            new MultipleRegulationsFactory(ImmutableMap.of(
                                StandardIattcRegulationsFactory.EL_CORRALITO_REG, TAG_FOR_ALL,
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(JULY, 15),
                                    EpoBiomassScenario.dayOfYear(OCTOBER, 8),
                                    new NoFishingFactory()
                                ), "closure A",
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(NOVEMBER, 9),
                                    EpoBiomassScenario.dayOfYear(FEBRUARY, 2),
                                    new NoFishingFactory()
                                ), "closure B"
                            ))
                        )
                    )
            ),
            makePolicy(
                "100-day closures",
                ImmutableList.of(currentFadLimits),
                scenario ->
                    new CompositeMultipleRegulationsFactory(
                        ImmutableList.of(
                            new ProtectedAreasFromFolderFactory(
                                scenario.getInputFolder().path("regions"),
                                "region_tags.csv"
                            ),
                            new MultipleRegulationsFactory(ImmutableMap.of(
                                StandardIattcRegulationsFactory.EL_CORRALITO_REG, TAG_FOR_ALL,
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(JULY, 1),
                                    EpoBiomassScenario.dayOfYear(OCTOBER, 8),
                                    new NoFishingFactory()
                                ), "closure A",
                                new TemporaryRegulationFactory(
                                    EpoBiomassScenario.dayOfYear(NOVEMBER, 9),
                                    EpoBiomassScenario.dayOfYear(FEBRUARY, 16),
                                    new NoFishingFactory()
                                ), "closure B"
                            ))
                        )
                    )
            ),
            makePolicy(
                "Business as usual",
                ImmutableList.of(currentFadLimits),
                null
            )
        );
    }

    private Policy<EpoBiomassScenario> makePolicy(
        final String policyName,
        final Collection<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulationFactories,
        final Function<EpoBiomassScenario, AlgorithmFactory<? extends Regulation>> makeGeneralRegulationFactory
    ) {
        return Policy.makeDelayedRegulationsPolicy(
            policyName,
            actionSpecificRegulationFactories,
            makeGeneralRegulationFactory,
            POLICY_KICK_IN_YEAR
        );
    }

}
