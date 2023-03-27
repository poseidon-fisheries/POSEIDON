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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.tuna.BiologicalProcessesFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.monitors.regions.CustomRegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.network.SocialNetwork;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.*;
import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class EpoScenario<B extends LocalBiology, F extends Fad<B, F>>
    implements TestableScenario {

    public static final MapExtent DEFAULT_MAP_EXTENT =
        MapExtent.from(101, 100, new Envelope(-171, -70, -50, 50));

    public static final RegionalDivision REGIONAL_DIVISION = new CustomRegionalDivision(
        DEFAULT_MAP_EXTENT,
        ImmutableMap.of(
            "West", entry(new Coordinate(-170.5, 49.5), new Coordinate(-140.5, -49.5)),
            "North", entry(new Coordinate(-139.5, 50), new Coordinate(-90.5, 0.5)),
            "South", entry(new Coordinate(-139.5, -0.5), new Coordinate(-90.5, -49.5)),
            "East", entry(new Coordinate(-89.5, 49.5), new Coordinate(-70.5, -49.5))
        )
    );

    public static final int TARGET_YEAR = 2017;
    public static final LocalDate START_DATE = LocalDate.of(TARGET_YEAR - 1, 1, 1);

    private InputPath inputFolder = InputPath.of("inputs", "epo_inputs");
    public SpeciesCodesFromFileFactory speciesCodesSupplier =
        new SpeciesCodesFromFileFactory(
            inputFolder.path("species_codes.csv")
        );
    private final InputPath testInputFolder = inputFolder.path("tests");
    private BiologicalProcessesFactory<B> biologicalProcessesFactory;
    private CurrentPatternMapSupplier currentPatternMapSupplier = new CurrentPatternMapSupplier(
        inputFolder,
        ImmutableMap.of(
            Y2016, Paths.get("currents", "currents_2016.csv"),
            Y2017, Paths.get("currents", "currents_2017.csv"),
            Y2018, Paths.get("currents", "currents_2018.csv")
        )
    );
    private FadMapFactory<B, F> fadMapFactory;
    private List<AlgorithmFactory<? extends Startable>> additionalStartables = new LinkedList<>();
    private AlgorithmFactory<? extends MapInitializer> mapInitializerFactory =
        new FromFileMapInitializerFactory(
            getInputFolder().path("depth.csv"),
            101,
            0.5
        );

    public static String getBoatId(final Fisher fisher) {
        return fisher.getTags().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Boat id not set for " + fisher));
    }

    public static int dayOfYear(final Month month, final int dayOfMonth) {
        return LocalDate.of(TARGET_YEAR, month, dayOfMonth)
            .getDayOfYear();
    }

    public BiologicalProcessesFactory<B> getBiologicalProcessesFactory() {
        return biologicalProcessesFactory;
    }

    public void setBiologicalProcessesFactory(final BiologicalProcessesFactory<B> biologicalProcessesFactory) {
        this.biologicalProcessesFactory = biologicalProcessesFactory;
    }

    public AlgorithmFactory<? extends MapInitializer> getMapInitializerFactory() {
        return mapInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setMapInitializerFactory(final AlgorithmFactory<? extends MapInitializer> mapInitializerFactory) {
        this.mapInitializerFactory = mapInitializerFactory;
    }

    public InputPath testFolder() {
        return testInputFolder;
    }

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier getCurrentPatternMapSupplier() {
        return currentPatternMapSupplier;
    }

    @SuppressWarnings("unused")
    public void setCurrentPatternMapSupplier(final CurrentPatternMapSupplier currentPatternMapSupplier) {
        this.currentPatternMapSupplier = currentPatternMapSupplier;
    }

    public SpeciesCodesFromFileFactory getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final SpeciesCodesFromFileFactory speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
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
        final FadMap<B, F> fadMap = getFadMapFactory().apply(fishState);
        fishState.setFadMap(fadMap);
        fishState.registerStartable(fadMap);

        additionalStartables.stream()
            .map(additionalStartable -> additionalStartable.apply(fishState))
            .forEach(fishState::registerStartable);

        return new ScenarioPopulation(
            makeFishers(fishState, TARGET_YEAR),
            new SocialNetwork(new EmptyNetworkBuilder()),
            ImmutableMap.of() // no entry in the fishery so no need to pass factory here
        );
    }

    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return ImmutableList.of();
    }

    public FadMapFactory<B, F> getFadMapFactory() {
        return this.fadMapFactory;
    }

    public void setFadMapFactory(final FadMapFactory<B, F> fadMapFactory) {
        this.fadMapFactory = fadMapFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public void useDummyData() {
        getFadMapFactory().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);
    }

    @Override
    public LocalDate getStartDate() {
        return START_DATE;
    }

    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends Startable>> getAdditionalStartables() {
        return additionalStartables;
    }

    @SuppressWarnings("unused")
    public void setAdditionalStartables(final List<AlgorithmFactory<? extends Startable>> additionalStartables) {
        this.additionalStartables = additionalStartables;
    }

    @Override
    public ScenarioEssentials start(final FishState fishState) {
        System.out.println("Starting model...");
        logCurrentTime(fishState);
        fishState.scheduleEveryDay(TunaCalibrator::logCurrentTime, StepOrder.DAWN);

        final NauticalMap nauticalMap =
            getMapInitializerFactory()
                .apply(fishState)
                .makeMap(fishState.random, null, fishState);

        final BiologicalProcessesFactory.Processes biologicalProcesses =
            biologicalProcessesFactory.initProcesses(nauticalMap, fishState);
        biologicalProcesses.startableFactories.forEach(getAdditionalStartables()::add);
        final GlobalBiology globalBiology = biologicalProcesses.globalBiology;

        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));
        nauticalMap.initializeBiology(biologicalProcesses.biologyInitializer, fishState.random, globalBiology);
        biologicalProcesses.biologyInitializer.processMap(globalBiology, nauticalMap, fishState.random, fishState);

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }
}
