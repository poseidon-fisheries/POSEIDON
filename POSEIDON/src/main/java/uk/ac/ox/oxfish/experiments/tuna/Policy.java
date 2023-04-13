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

import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Policy<S extends Scenario> {

    public static final Policy<Scenario> DEFAULT = new Policy<>(
        "Default",
        "No changes to the default scenario",
        scenario -> {
        }
    );

    private final String name;
    private final String description;
    private final Consumer<S> scenarioConsumer;

    public Policy(
        final String name,
        final String description,
        final Consumer<S> scenarioConsumer
    ) {
        this.name = name;
        this.description = description;
        this.scenarioConsumer = scenarioConsumer;
    }

    public Policy(
        final String name,
        final Consumer<S> scenarioConsumer
    ) {
        this.name = name;
        this.description = name;
        this.scenarioConsumer = scenarioConsumer;
    }

    /*
     * The reason S extends EpoScenario instead of just Scenario is that it's where `addPlugin` is defined.
     * That should be cleaned up, but there is no time for that now. There seldom is. Sigh.
     */
    public static <S extends EpoScenario<?>> Policy<S> makeDelayedRegulationsPolicy(
        final String policyName,
        final Collection<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulationFactories,
        final Function<S, AlgorithmFactory<? extends Regulation>> makeGeneralRegulationFactory,
        final int yearsBeforePoliciesKickIn
    ) {
        final Consumer<S> scenarioConsumer = scenario -> {
            final Optional<AlgorithmFactory<? extends Regulation>> generalRegulationFactory =
                Optional.ofNullable(makeGeneralRegulationFactory).map(factory -> factory.apply(scenario));
            final Steppable setRegulations = simState -> {
                final FishState fishState = (FishState) simState;
                System.out.println(
                    "Setting regulations to " + policyName + " for all fishers at day " +
                        simState.schedule.getSteps()
                );
                fishState.getFishers().forEach(fisher -> {
                    generalRegulationFactory.map(factory -> factory.apply(fishState)).ifPresent(fisher::setRegulation);
                    if (actionSpecificRegulationFactories != null) {
                        ((PurseSeineGear) fisher.getGear()).getFadManager().setActionSpecificRegulations(
                            actionSpecificRegulationFactories.stream().map(factory -> factory.apply(fishState))
                        );
                    }
                });
            };
            scenario.getAdditionalStartables().add(__ -> fishState ->
                fishState.scheduleOnceAtTheBeginningOfYear(
                    setRegulations,
                    StepOrder.AFTER_DATA,
                    yearsBeforePoliciesKickIn
                )
            );
        };
        return new Policy<>(policyName, policyName, scenarioConsumer);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Consumer<S> getScenarioConsumer() {
        return scenarioConsumer;
    }

}
