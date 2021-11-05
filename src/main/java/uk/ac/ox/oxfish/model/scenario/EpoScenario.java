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

import static com.google.common.collect.ImmutableRangeMap.toImmutableRangeMap;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.Y2017;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EARNINGS;
import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.VARIABLE_COSTS;
import static uk.ac.ox.oxfish.model.scenario.StandardIattcRegulationsFactory.scheduleClosurePeriodChoice;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import javax.measure.quantity.Mass;
import org.jetbrains.annotations.NotNull;
import tech.units.indriya.ComparableQuantity;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.HourlyCost;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscardingFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.factory.IgnoreWeatherFactory;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.geography.fads.FadMapFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class EpoScenario<B extends LocalBiology, F extends Fad<B, F>> implements Scenario {

    public static final int TARGET_YEAR = 2017;
    public static final Path INPUT_PATH = Paths.get("inputs", "epo");
    public static final SpeciesCodesFromFileFactory speciesCodesSupplier =
        new SpeciesCodesFromFileFactory(INPUT_PATH.resolve("species_codes.csv"));
    static final ImmutableMap<CurrentPattern, Path> currentFiles =
        new ImmutableMap.Builder<CurrentPattern, Path>()
            //.put(Y2015, input("currents_2015.csv"))
            //.put(Y2016, input("currents_2016.csv"))
            .put(Y2017, INPUT_PATH.resolve("currents").resolve("currents_2017.csv"))
            //.put(Y2018, input("currents_2018.csv"))
            //.put(NEUTRAL, input("currents_neutral.csv"))
            //.put(EL_NINO, input("currents_el_nino.csv"))
            //.put(LA_NINA, input("currents_la_nina.csv"))
            .build();
    private Path costsFile = INPUT_PATH.resolve("costs.csv");

    void initModel(final FishState fishState) {
        final FadMap<B, F> fadMap = getFadMapFactory().apply(fishState);
        fishState.setFadMap(fadMap);
        fishState.registerStartable(fadMap);
    }

    abstract FadMapFactory<B, F> getFadMapFactory();

    @NotNull
    FisherFactory makeFisherFactory(
        final FishState fishState,
        final PurseSeineGearFactory<B, F> purseSeineGearFactory,
        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory,
        final AlgorithmFactory<? extends FishingStrategy> fishingStrategyFactory
    ) {
        final FisherFactory fisherFactory = new FisherFactory(
            null,
            new StandardIattcRegulationsFactory(),
            new PurseSeinerDepartingStrategyFactory(),
            gravityDestinationStrategyFactory,
            fishingStrategyFactory,
            new NoDiscardingFactory(),
            new FadRefillGearStrategyFactory(),
            new IgnoreWeatherFactory(),
            null,
            null,
            purseSeineGearFactory,
            0
        );

        fisherFactory.getAdditionalSetups().addAll(ImmutableList.of(
            addHourlyCosts(),
            fisher -> ((PurseSeineGear<?, ?>) fisher.getGear()).getFadManager().setFisher(fisher),
            fisher -> scheduleClosurePeriodChoice(fishState, fisher),
            fisher -> fisher.getYearlyData().registerGatherer(
                "Profits",
                fisher1 -> fisher1.getYearlyCounterColumn(EARNINGS)
                    - fisher1.getYearlyCounterColumn(VARIABLE_COSTS),
                Double.NaN
            ),
            fisher -> fisher.getYearlyCounter().addColumn("Distance travelled"),
            fisher -> fisher.getYearlyData().registerGatherer("Distance travelled", fisher1 ->
                fisher1.getYearlyCounterColumn("Distance travelled"), Double.NaN
            ),
            fisher -> fisher.addTripListener((tripRecord, fisher1) ->
                fisher1.getYearlyCounter()
                    .count("Distance travelled", tripRecord.getDistanceTravelled())
            )
        ));

        return fisherFactory;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Consumer<Fisher> addHourlyCosts() {
        final RangeMap<ComparableQuantity<Mass>, HourlyCost> hourlyCostsPerCarryingCapacity =
            parseAllRecords(costsFile).stream().collect(toImmutableRangeMap(
                r -> Range.openClosed(
                    getQuantity(r.getInt("lower_capacity"), TONNE),
                    getQuantity(r.getInt("upper_capacity"), TONNE)
                ),
                r -> new HourlyCost(r.getDouble("daily_cost") / 24.0)
            ));
        // Setup hourly costs as a function of capacity
        return fisher -> {
            final ComparableQuantity<Mass> capacity =
                getQuantity(fisher.getHold().getMaximumLoad(), KILOGRAM);
            final HourlyCost hourlyCost = hourlyCostsPerCarryingCapacity.get(capacity);
            fisher.getAdditionalTripCosts().add(hourlyCost);
        };
    }

    @SuppressWarnings("unused")
    public Path getCostsFile() {
        return costsFile;
    }

    public void setCostsFile(final Path costsFile) {
        this.costsFile = costsFile;
    }


}
