/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.epo.scenarios;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.tuna.BiologicalProcesses;
import uk.ac.ox.oxfish.biology.tuna.BiologicalProcessesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EmptyFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.DefaultEpoRegulations;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadZapperFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.ScenarioEssentials;
import uk.ac.ox.oxfish.model.scenario.ScenarioPopulation;
import uk.ac.ox.oxfish.model.scenario.TestableScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtentFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static uk.ac.ox.oxfish.utility.Dummyable.maybeUseDummyData;

public abstract class EpoScenario<B extends LocalBiology>
    implements TestableScenario {

    private static final Logger logger = Logger.getLogger(EpoScenario.class.getName());
    private MapExtentFactory mapExtentFactory =
        new MapExtentFactory(
            101, 100, -171, -70, -50, 50
        );
    private IntegerParameter targetYear = new IntegerParameter(2022);
    private InputPath inputFolder = InputPath.of("epo_inputs");
    private final InputPath testInputFolder = inputFolder.path("tests");
    private Map<String, AlgorithmFactory<? extends Startable>> additionalStartables =
        new HashMap<>(ImmutableMap.of(
            "FAD zapper", new FadZapperFactory(
                new FixedDoubleParameter(300),
                new IntegerParameter(20)
            )
        ));
    private ComponentFactory<? extends Regulations> regulations = DefaultEpoRegulations.make(
        getInputFolder());
    private BiologicalProcessesFactory<B> biologicalProcesses;
    private CurrentPatternMapSupplier currentPatternMapSupplier = new CurrentPatternMapSupplier(
        inputFolder,
        ImmutableMap.of(
            "Y2021", Paths.get("currents", "currents_2021.csv"),
            "Y2022", Paths.get("currents", "currents_2022.csv"),
            "Y2023", Paths.get("currents", "currents_2017.csv") // using 2017 as proxy for 2023
        )
    );
    private FadMapFactory fadMap;
    private AlgorithmFactory<? extends MapInitializer> mapInitializer =
        new FromFileMapInitializerFactory(
            getInputFolder().path("depth.csv"),
            101,
            0.5
        );
    private AlgorithmFactory<ScenarioPopulation> fleet = new EmptyFleetFactory();

    public static int dayOfYear(
        final int year,
        final Month month,
        final int dayOfMonth
    ) {
        return LocalDate.of(year, month, dayOfMonth).getDayOfYear();
    }

    public MapExtentFactory getMapExtentFactory() {
        return mapExtentFactory;
    }

    public void setMapExtentFactory(final MapExtentFactory mapExtentFactory) {
        this.mapExtentFactory = mapExtentFactory;
    }

    public ComponentFactory<? extends Regulations> getRegulations() {
        return regulations;
    }

    public void setRegulations(final ComponentFactory<? extends Regulations> regulations) {
        this.regulations = regulations;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    public BiologicalProcessesFactory<B> getBiologicalProcesses() {
        return biologicalProcesses;
    }

    public void setBiologicalProcesses(final BiologicalProcessesFactory<B> biologicalProcesses) {
        this.biologicalProcesses = biologicalProcesses;
    }

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier getCurrentPatternMapSupplier() {
        return currentPatternMapSupplier;
    }

    @SuppressWarnings("unused")
    public void setCurrentPatternMapSupplier(final CurrentPatternMapSupplier currentPatternMapSupplier) {
        this.currentPatternMapSupplier = currentPatternMapSupplier;
    }

    @SuppressWarnings("unused")
    public InputPath getInputFolder() {
        return inputFolder;
    }

    @SuppressWarnings("unused")
    public void setInputFolder(final InputPath inputPath) {
        this.inputFolder = inputPath;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        final FadMap fadMap = getFadMap().apply(fishState);
        fishState.setFadMap(fadMap);
        fishState.registerStartable(fadMap);

        additionalStartables.values().stream()
            .map(additionalStartable -> additionalStartable.apply(fishState))
            .forEach(fishState::registerStartable);

        return fleet.apply(fishState);
    }

    public FadMapFactory getFadMap() {
        return this.fadMap;
    }

    public void setFadMap(final FadMapFactory fadMap) {
        this.fadMap = fadMap;
    }

    @SuppressWarnings("unused")
    @Override
    public void useDummyData() {
        getFadMap().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);
        maybeUseDummyData(testFolder(), getFleet());
    }

    public InputPath testFolder() {
        return testInputFolder;
    }

    public AlgorithmFactory<ScenarioPopulation> getFleet() {
        return fleet;
    }

    public void setFleet(final AlgorithmFactory<ScenarioPopulation> fleet) {
        this.fleet = fleet;
    }

    @Override
    public LocalDate getStartDate() {
        return LocalDate.of(targetYear.getValue() - 1, 1, 1);
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {
        logger.info("Starting model...");

        final NauticalMap nauticalMap =
            getMapInitializer()
                .apply(fishState)
                .makeMap(fishState.random, null, fishState);

        final BiologicalProcesses biologicalProcesses =
            this.biologicalProcesses.apply(fishState);

        final GlobalBiology globalBiology =
            biologicalProcesses.getGlobalBiology();

        biologicalProcesses.getStartableFactories().forEach(bpf ->
            getAdditionalStartables().put(bpf.toString(), bpf)
        );
        nauticalMap.setPathfinder(
            new AStarFallbackPathfinder(nauticalMap.getDistance())
        );
        nauticalMap.initializeBiology(
            biologicalProcesses.getBiologyInitializer(),
            fishState.random,
            globalBiology
        );
        biologicalProcesses
            .getBiologyInitializer()
            .processMap(globalBiology, nauticalMap, fishState.random, fishState);

        return new ScenarioEssentials(
            globalBiology,
            nauticalMap
        );
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializer() {
        return mapInitializer;
    }

    @SuppressWarnings("unused")
    public void setMapInitializer(final AlgorithmFactory<? extends MapInitializer> mapInitializer) {
        this.mapInitializer = mapInitializer;
    }

    public Map<String, AlgorithmFactory<? extends Startable>> getAdditionalStartables() {
        return additionalStartables;
    }

    public void setAdditionalStartables(final Map<String, AlgorithmFactory<? extends Startable>> additionalStartables) {
        this.additionalStartables = additionalStartables;
    }
}
