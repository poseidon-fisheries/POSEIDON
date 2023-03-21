/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishUntilFullFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.*;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class FadsOnlyEpoAbundanceScenario extends EpoScenario<Entry<String, SizeGroup>, AbundanceLocalBiology, AbundanceFad> {

    private boolean fadSettingActive = true;

    private AlgorithmFactory<? extends AdditionalStartable> fadMakerFactory =
        new ExogenousFadMakerCSVFactory(
            getInputFolder().path("calibration", "fad_deployments.csv"),
            new AbundanceFadInitializerFactory()
        );

    private AlgorithmFactory<? extends AdditionalStartable> fadSetterFactory =
        new ExogenousFadSetterCSVFactory(
            getInputFolder().path("calibration", "fad_sets.csv"), true
        );

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );
    private AlgorithmFactory<? extends FadInitializer>
        fadInitializerFactory =
        new AbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );

    public FadsOnlyEpoAbundanceScenario() {
        setBiologicalProcessesFactory(
            new AbundanceProcessesFactory(getInputFolder().path("abundance"), getSpeciesCodesSupplier())
        );
        this.setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
        this.setFishingStrategyFactory(new FishUntilFullFactory());
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AdditionalStartable> getFadMakerFactory() {
        return fadMakerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadMakerFactory(final AlgorithmFactory<? extends AdditionalStartable> fadMakerFactory) {
        this.fadMakerFactory = fadMakerFactory;
    }

    @SuppressWarnings("unused")
    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        final Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
            abundanceFilters =
            abundanceFiltersFactory.apply(fishState);

        if (fadInitializerFactory instanceof AbundanceFadInitializerFactory) {
            ((FadInitializerFactory<AbundanceLocalBiology, AbundanceFad>) fadInitializerFactory)
                .setSpeciesCodesSupplier(getSpeciesCodesSupplier());
        }
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(abundanceFilters.get(
            FadSetAction.class));
        ((ExogenousFadMakerCSVFactory) fadMakerFactory).setFadInitializer(fadInitializerFactory);

        fishState.registerStartable(fadMakerFactory.apply(fishState));
        if (fadSettingActive)
            fishState.registerStartable(fadSetterFactory.apply(fishState));

        return scenarioPopulation;
    }

    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        ((ExogenousFadMakerCSVFactory) fadMakerFactory).setDeploymentsFile(
            testFolder().path("dummy_fad_deployments.csv")
        );
        ((ExogenousFadSetterCSVFactory) fadSetterFactory).setSetsFile(
            testFolder().path("dummy_fad_sets.csv")
        );
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends AdditionalStartable> getFadSetterFactory() {
        return fadSetterFactory;
    }

    @SuppressWarnings("unused")
    public void setFadSetterFactory(
        final AlgorithmFactory<? extends AdditionalStartable> fadSetterFactory
    ) {
        this.fadSetterFactory = fadSetterFactory;
    }

    public boolean isFadSettingActive() {
        return fadSettingActive;
    }

    public void setFadSettingActive(boolean fadSettingActive) {
        this.fadSettingActive = fadSettingActive;
    }

}
