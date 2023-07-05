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

package uk.ac.ox.oxfish.fisher.purseseiner;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedRestTimeDepartingStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static si.uom.NonSI.KNOT;
import static si.uom.NonSI.TONNE;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.*;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class PurseSeineVesselReader implements AlgorithmFactory<List<Fisher>> {

    private final Path vesselsFilePath;
    private final int targetYear;
    private final FisherFactory fisherFactory;
    private final Map<String, Port> portsByName;
    private final Supplier<FuelTank> fuelTankSupplier = () -> new FuelTank(Double.MAX_VALUE);

    public PurseSeineVesselReader(
        final Path vesselsFilePath,
        final int targetYear,
        final FisherFactory fisherFactory,
        final Collection<Port> ports
    ) {
        this.vesselsFilePath = vesselsFilePath;
        this.targetYear = targetYear;
        this.fisherFactory = fisherFactory;
        this.portsByName = ports.stream().collect(toImmutableMap(Port::getName, identity()));
    }

    /**
     * Recursively find fixed rest time departing strategies and set minimumHoursToWait.
     */
    private static void setFixedRestTime(
        final DepartingStrategy departingStrategy,
        final double minimumHoursToWait
    ) {
        if (departingStrategy instanceof FixedRestTimeDepartingStrategy) {
            ((FixedRestTimeDepartingStrategy) departingStrategy).setMinimumHoursToWait(
                minimumHoursToWait);
        } else if (departingStrategy instanceof CompositeDepartingStrategy) {
            ((CompositeDepartingStrategy) departingStrategy).getStrategies()
                .forEach(s -> setFixedRestTime(s, minimumHoursToWait));
        }
    }

    public static void chooseClosurePeriod(final Fisher fisher, final String closure) {
        final ImmutableList<String> periods = ImmutableList.of("closure A", "closure B");
        final String tag = "closure " + closure;
        checkArgument(periods.contains(tag));
        fisher.getTagsList().removeIf(periods::contains);
        fisher.getTagsList().add(tag);
    }

    @Override
    public List<Fisher> apply(final FishState fishState) {

        return recordStream(vesselsFilePath)
            .filter(record -> record.getInt("year") == targetYear)
            .map(record -> {
                final String portName = record.getString("port_name");
                final Double length = record.getDouble("length_in_m");
                final Quantity<Mass> carryingCapacity =
                    getQuantity(record.getDouble("carrying_capacity_in_t"), TONNE);
                final double carryingCapacityInKg = asDouble(carryingCapacity, KILOGRAM);
                final Quantity<Volume> holdVolume =
                    getQuantity(record.getDouble("hold_volume_in_m3"), CUBIC_METRE);
                final Quantity<Speed> speed =
                    getQuantity(record.getDouble("speed_in_knots"), KNOT);
                final Engine engine = new Engine(
                    Double.NaN, // Unused
                    1.0, // This is not realistic, but fuel costs are wrapped into daily costs
                    asDouble(speed, KILOMETRE_PER_HOUR)
                );
                fisherFactory.setPortSupplier(() -> portsByName.get(portName));
                // we don't have beam width in the data file, but it isn't used anyway
                final double beam = 1.0;
                fisherFactory.setBoatSupplier(() -> new Boat(
                    length,
                    beam,
                    engine,
                    fuelTankSupplier.get()
                ));
                fisherFactory.setHoldSupplier(() -> new Hold(
                    carryingCapacityInKg,
                    holdVolume,
                    fishState.getBiology()
                ));
                final String boatId = record.getString("ves_no");
                final Fisher fisher = fisherFactory.buildFisher(fishState);
                fisher.getTagsList().add(boatId);
                setFixedRestTime(
                    fisher.getDepartingStrategy(),
                    record.getDouble("mean_time_at_port_in_hours")
                );
                if (record.getBoolean("has_del_license")) {
                    fisher.getTagsList().add("has_del_license");
                }
                if (record.getMetaData().containsColumn("closure")) {
                    chooseClosurePeriod(fisher, record.getString("closure"));
                }
                if (record.getMetaData().containsColumn("flag")) {
                    fisher.getTagsList().add(record.getString("flag"));
                }
                // TODO: setMaxTravelTime(fisher, record.getDouble
                //  ("max_trip_duration_in_hours"));
                return fisher;
            })
            .collect(toImmutableList());
    }

}
