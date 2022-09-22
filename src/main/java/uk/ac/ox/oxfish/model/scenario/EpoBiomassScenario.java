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

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.tuna.*;
import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.biology.weather.initializer.factory.ConstantWeatherFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.BiomassCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.FromFileMapInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.event.ExogenousCatches;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.ac.ox.oxfish.utility.Measures.DOLLAR;

/**
 * The biomass-based IATTC tuna simulation scenario.
 */
public class EpoBiomassScenario extends EpoScenario<BiomassLocalBiology, BiomassFad> {

    private final List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new ArrayList<>();
    private final BiomassReallocatorFactory biomassReallocatorFactory =
        new BiomassReallocatorFactory(
            INPUT_PATH.resolve("biomass").resolve("biomass_distributions.csv"),
            365
        );
    private Path attractionWeightsFile = INPUT_PATH.resolve("action_weights.csv");
    private Path mapFile = INPUT_PATH.resolve("depth.csv");
    private boolean fadMortalityIncludedInExogenousCatches = true;
    private final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory =
        new BiomassDrivenTimeSeriesExogenousCatchesFactory(
            INPUT_PATH.resolve("biomass").resolve("exogenous_catches.csv"),
            TARGET_YEAR,
            fadMortalityIncludedInExogenousCatches
        );
    private FromFileMapInitializerFactory mapInitializer =
        new FromFileMapInitializerFactory(mapFile, 101, 0.5);
    private AlgorithmFactory<? extends WeatherInitializer> weatherInitializer =
        new ConstantWeatherFactory();
    private DoubleParameter gasPricePerLiter = new FixedDoubleParameter(0.01);
    private BiomassInitializerFactory biomassInitializerFactory = new BiomassInitializerFactory();
    private BiomassRestorerFactory biomassRestorerFactory = new BiomassRestorerFactory();
    private ScheduledBiomassProcessesFactory
        scheduledBiomassProcessesFactory = new ScheduledBiomassProcessesFactory();
    private AlgorithmFactory<? extends FadInitializer>
        fadInitializerFactory = new BiomassFadInitializerFactory(
        // use numbers from https://github.com/poseidon-fisheries/tuna/blob/9c6f775ced85179ec39e12d8a0818bfcc2fbc83f/calibration/results/ernesto/best_base_line/calibrated_scenario.yaml
        ImmutableMap.of(
            "Bigeye tuna", 0.7697766896339598,
            "Yellowfin tuna", 1.1292389959739901,
            "Skipjack tuna", 0.0
        ),
        ImmutableMap.of(
            "Bigeye tuna", 1.0184011081061861,
            "Yellowfin tuna", 0.0,
            "Skipjack tuna", 0.7138646301498129
        ),
        ImmutableMap.of(
            "Bigeye tuna", 9.557509707646096,
            "Yellowfin tuna", 10.419783885948643,
            "Skipjack tuna", 9.492481930328207
        ),
        ImmutableMap.of(
            "Bigeye tuna", 0.688914118975473,
            "Yellowfin tuna", 0.30133562299610883,
            "Skipjack tuna", 1.25
        )
    );

    private AlgorithmFactory<? extends Regulation> regulationsFactory =
        new StandardIattcRegulationsFactory();

    public EpoBiomassScenario() {
        setFadMapFactory(new BiomassFadMapFactory(currentFiles));
        setFishingStrategyFactory(new PurseSeinerBiomassFishingStrategyFactory());
        setCatchSamplersFactory(new BiomassCatchSamplersFactory());
        setPurseSeineGearFactory(new BiomassPurseSeineGearFactory());
    }

    public static String getBoatId(final Fisher fisher) {
        return fisher.getTags().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Boat id not set for " + fisher));
    }

    public static int dayOfYear(final Month month, final int dayOfMonth) {
        return LocalDate.of(TARGET_YEAR, month, dayOfMonth)
            .getDayOfYear();
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends Regulation> getRegulationsFactory() {
        return regulationsFactory;
    }

    @SuppressWarnings("unused")
    public void setRegulationsFactory(final AlgorithmFactory<? extends Regulation> regulationsFactory) {
        this.regulationsFactory = regulationsFactory;
    }

    @SuppressWarnings("unused")
    public BiomassRestorerFactory getMultiSpeciesBiomassRestorerFactory() {
        return biomassRestorerFactory;
    }

    @SuppressWarnings("unused")
    public void setMultiSpeciesBiomassRestorerFactory(final BiomassRestorerFactory biomassRestorerFactory) {
        this.biomassRestorerFactory = biomassRestorerFactory;
    }

    @SuppressWarnings("unused")
    public ScheduledBiomassProcessesFactory getBiomassReallocatorFactory() {
        return scheduledBiomassProcessesFactory;
    }

    @SuppressWarnings("unused")
    public void setBiomassReallocatorFactory(final ScheduledBiomassProcessesFactory scheduledBiomassProcessesFactory) {
        this.scheduledBiomassProcessesFactory = scheduledBiomassProcessesFactory;
    }

    @SuppressWarnings("unused")
    public Path getMapFile() {
        return mapFile;
    }

    @SuppressWarnings("unused")
    public void setMapFile(final Path mapFile) {
        this.mapFile = mapFile;
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory getExogenousCatchesFactory() {
        return exogenousCatchesFactory;
    }

    public FromFileMapInitializerFactory getMapInitializer() {
        return mapInitializer;
    }

    public void setMapInitializer(
        final FromFileMapInitializerFactory mapInitializer
    ) {
        this.mapInitializer = mapInitializer;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends WeatherInitializer> getWeatherInitializer() {
        return weatherInitializer;
    }

    @SuppressWarnings("unused")
    public void setWeatherInitializer(
        final AlgorithmFactory<? extends WeatherInitializer> weatherInitializer
    ) {
        this.weatherInitializer = weatherInitializer;
    }

    public DoubleParameter getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(final DoubleParameter gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    @Override
    public ScenarioEssentials start(final FishState model) {

        System.out.println("Starting model...");

        final NauticalMap nauticalMap =
            mapInitializer.apply(model).makeMap(model.random, null, model);

        final SpeciesCodes speciesCodes = speciesCodesSupplier.get();
        biomassReallocatorFactory.setSpeciesCodes(speciesCodes);
        biomassReallocatorFactory.setMapExtent(new MapExtent(nauticalMap));
        final BiomassReallocator biomassReallocator = biomassReallocatorFactory.apply(model);
        scheduledBiomassProcessesFactory.setBiomassReallocator(biomassReallocator);

        plugins.add(scheduledBiomassProcessesFactory);

        biomassRestorerFactory.setBiomassReallocator(biomassReallocator);
        plugins.add(biomassRestorerFactory);

        biomassInitializerFactory.setBiomassReallocator(biomassReallocator);

        final BiomassInitializer biologyInitializer = biomassInitializerFactory.apply(model);
        final GlobalBiology globalBiology = biologyInitializer.generateGlobal(model.random, model);
        nauticalMap.setPathfinder(new AStarFallbackPathfinder(nauticalMap.getDistance()));

        //this next static method calls biology.initialize, weather.initialize and the like
        NauticalMapFactory.initializeMap(
            nauticalMap,
            model.random,
            biologyInitializer,
            this.weatherInitializer.apply(model),
            globalBiology,
            model
        );

        return new ScenarioEssentials(globalBiology, nauticalMap);
    }

    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends AdditionalStartable>> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public void addPlugin(final AlgorithmFactory<? extends AdditionalStartable> plugin) {
        plugins.add(plugin);
    }

    @SuppressWarnings("unused")
    public boolean isFadMortalityIncludedInExogenousCatches() {
        return fadMortalityIncludedInExogenousCatches;
    }

    @SuppressWarnings("unused")
    public void setFadMortalityIncludedInExogenousCatches(final boolean fadMortalityIncludedInExogenousCatches) {
        this.fadMortalityIncludedInExogenousCatches = fadMortalityIncludedInExogenousCatches;
    }

    @SuppressWarnings("unused")
    public BiomassInitializerFactory getBiomassReallocatorInitializerFactory() {
        return biomassInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setBiomassReallocatorInitializerFactory(final BiomassInitializerFactory biomassInitializerFactory) {
        this.biomassInitializerFactory = biomassInitializerFactory;
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {

        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
            new GravityDestinationStrategyFactory();
        gravityDestinationStrategyFactory.setAttractionWeightsFile(getAttractionWeightsFile());
        gravityDestinationStrategyFactory.setMaxTripDurationFile(getVesselsFilePath());

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            regulationsFactory,
            gravityDestinationStrategyFactory
        );

        fishState.getYearlyDataSet().registerGatherer(
            "Total profits",
            model -> model.getFishers()
                .stream()
                .mapToDouble(fisher -> fisher.getLatestYearlyObservation("Profits"))
                .sum(),
            Double.NaN,
            DOLLAR,
            "Profits"
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFilePath(),
                TARGET_YEAR,
                fisherFactory,
                buildPorts(fishState)
            ).apply(fishState);

        exogenousCatchesFactory.setSpeciesCodes(speciesCodesSupplier.get());
        final ExogenousCatches exogenousCatches = exogenousCatchesFactory.apply(fishState);
        fishState.registerStartable(exogenousCatches);

        plugins.forEach(plugin -> fishState.registerStartable(plugin.apply(fishState)));

        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    @Override
    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }


}
