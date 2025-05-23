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

package uk.ac.ox.poseidon.epo.monitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.*;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.*;
import uk.ac.ox.oxfish.model.data.monitors.regions.CustomRegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Mass;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.concat;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

@SuppressWarnings("rawtypes")
public class DefaultEpoMonitors {

    private final RegionalDivision regionalDivision;
    private final Collection<Monitor<FadDeploymentAction, ?, ?>> fadDeploymentMonitors;
    private final Collection<Monitor<AbstractSetAction, ?, ?>> allSetsMonitors;
    private final Collection<Monitor<AbstractFadSetAction, ?, ?>> fadSetMonitors;
    private final Collection<Monitor<NonAssociatedSetAction, ?, ?>> nonAssociatedSetMonitors;
    private final Collection<Monitor<DolphinSetAction, ?, ?>> dolphinSetMonitors;
    private final BiomassLostMonitor biomassLostMonitor;
    private final Collection<Monitor<?, ?, ?>> otherMonitors;

    public DefaultEpoMonitors(final FishState fishState) {

        regionalDivision = new CustomRegionalDivision(
            fishState.getMapExtent(),
            ImmutableMap.of(
                "West", entry(new Coordinate(-170.5, 49.5), new Coordinate(-140.5, -49.5)),
                "North", entry(new Coordinate(-139.5, 50), new Coordinate(-90.5, 0.5)),
                "South", entry(new Coordinate(-139.5, -0.5), new Coordinate(-90.5, -49.5)),
                "East", entry(new Coordinate(-89.5, 49.5), new Coordinate(-70.5, -49.5))
            )
        );

        final FishStateYearlyTimeSeries yearlyTimeSeries = fishState.getYearlyDataSet();

        fadDeploymentMonitors = ImmutableList.of(
            makeActionCounter("FAD deployments")
        );

        final Function<RegionalDivision.Region, ProportionalGatherer<Boolean,
            AbstractFadSetAction, AbstractFadSetAction, Dimensionless>>
            makeProportionOfSetsOnOwnFadsMonitor =
            region -> new ProportionalGatherer<>(basicGroupingMonitor(
                null,
                // we already have the total number of sets and don't want to gather it from here
                EVERY_YEAR,
                IncrementingAccumulator::new,
                AbstractUnit.ONE,
                "Number of sets",
                ImmutableList.of(true, false),
                isOnOwnFad -> String.format(
                    "sets on %s FADs%s",
                    isOnOwnFad ? "own" : "others'",
                    region == null ? "" : " (" + region + ")"
                ),
                fadSet -> ImmutableList.of(fadSet.isOwnFad()),
                __ -> identity()
            ));
        final GroupingMonitor<Species, AbstractFadSetAction, Double, Mass>
            catchFromFadSetsMonitor = perSpeciesMonitor(
            "catches from FAD sets",
            EVERY_YEAR,
            SummingAccumulator::new,
            Units.KILOGRAM,
            "Biomass",
            fishState.getSpecies(),
            species -> basicGroupingMonitor(
                species + " catches from FAD sets",
                EVERY_YEAR,
                SummingAccumulator::new,
                Units.KILOGRAM,
                "Biomass",
                ImmutableList.of(true, false),
                isOnOwnFad -> String.format(
                    "%s catches from sets on %s FADs",
                    species,
                    isOnOwnFad ? "own" : "others'"
                ),
                fadSet -> ImmutableList.of(fadSet.isOwnFad()),
                __ -> action -> action.getCatchesKept()
                    .map(catchesKept -> catchesKept.getWeightCaught(species))
                    .orElse(0.0)
            )
        );

        allSetsMonitors = ImmutableList.of(
            makeCatchFromSetAccumulator(
                fishState,
                "catches",
                SummingAccumulator::new
            )
        );

        fadSetMonitors = ImmutableList.of(
            this.makeActionCounter("FAD sets"),
            catchFromFadSetsMonitor,
            this.makeCatchFromSetAccumulator(
                fishState, "catches by FAD sets", IterativeAveragingAccumulator::new),
            makeProportionOfSetsOnOwnFadsMonitor.apply(null),
            GroupingMonitor.perRegionMonitor(
                null,
                EVERY_YEAR,
                regionalDivision,
                IncrementingAccumulator::new,
                AbstractUnit.ONE,
                "Proportion of sets",
                makeProportionOfSetsOnOwnFadsMonitor
            ),
            new BasicMonitor<>(
                "FAD soak time",
                EVERY_YEAR,
                IterativeAveragingAccumulator::new,
                Units.DAY,
                "Soak time",
                fadSet -> fadSet.getStep() - fadSet.getFad().getStepDeployed()
            ),
            new BasicMonitor<>(
                "FAD and OFS sets in southern area",
                EVERY_YEAR,
                SummingAccumulator::new,
                AbstractUnit.ONE,
                "Number of sets",
                fadSet -> fadSet
                    .getCoordinate()
                    .filter(coordinate ->
                        coordinate.x >= -125 && coordinate.x <= -80 &&
                            coordinate.y >= -20 && coordinate.y <= 0
                    )
                    .map(__ -> 1)
                    .orElse(0)
            ),
            GroupingMonitor.basicPerRegionMonitor(
                "sets on FADs deployed during current trip",
                EVERY_YEAR,
                regionalDivision,
                region -> fadSet -> fadSet.getFisher() != null &&
                    fadSet.getFisher().getCurrentTrip() == fadSet.getFad().getTripDeployed(),
                ProportionAccumulator::new,
                AbstractUnit.ONE,
                "Proportion of sets"
            )
        );

        nonAssociatedSetMonitors = ImmutableList.of(
            makeActionCounter("non-associated sets"),
            makeCatchFromSetAccumulator(
                fishState, "catches from non-associated sets", SummingAccumulator::new),
            makeCatchFromSetAccumulator(
                fishState, "catches by non-associated sets", IterativeAveragingAccumulator::new)
        );

        dolphinSetMonitors = ImmutableList.of(
            makeActionCounter("dolphin sets"),
            makeCatchFromSetAccumulator(
                fishState, "catches from dolphin sets", SummingAccumulator::new),
            makeCatchFromSetAccumulator(
                fishState, "catches by dolphin sets", IterativeAveragingAccumulator::new)
        );

        biomassLostMonitor = new BiomassLostMonitor(fishState.getSpecies());

        otherMonitors = ImmutableList.of(
            new ObservingAtIntervalMonitor<>(
                EVERY_YEAR,
                model -> model.getFadMap().allFads()::iterator,
                basicPerRegionMonitor(
                    "active FADs",
                    EVERY_YEAR,
                    regionalDivision,
                    region -> identity(),
                    IncrementingAccumulator::new,
                    AbstractUnit.ONE,
                    "Number of FADs"
                )
            ),
            new ObservingAtIntervalMonitor<>(
                EVERY_YEAR,
                model -> model.getFadMap().allFads()::iterator,
                perSpeciesPerRegionMonitor(
                    "biomass under FADs",
                    EVERY_YEAR,
                    SummingAccumulator::new,
                    Units.KILOGRAM,
                    "Biomass",
                    fishState.getSpecies(),
                    species -> region -> fad -> fad.getBiology().getBiomass(species),
                    regionalDivision
                )
            ),
            new ObservingAtIntervalMonitor<>(
                EVERY_YEAR,
                model -> model.getFadMap().allFads()::iterator,
                new BasicMonitor<>(
                    "days before attraction",
                    EVERY_YEAR,
                    IterativeAveragingAccumulator::new,
                    Units.DAY,
                    "Days",
                    Fad::getStepsBeforeFirstAttraction
                )
            )
        );

        getMonitors().forEach(monitor -> monitor.registerWith(yearlyTimeSeries));

    }

    private <E extends PurseSeinerAction> ProportionalGatherer<RegionalDivision.Region, E, E,
        Dimensionless> makeActionCounter(
        final String actionName
    ) {
        return new ProportionalGatherer<>(basicPerRegionMonitor(
            actionName,
            EVERY_YEAR,
            regionalDivision,
            region -> identity(),
            IncrementingAccumulator::new,
            AbstractUnit.ONE,
            "Number of " + actionName
        ));
    }

    private <A extends AbstractSetAction> GroupingMonitor<Species, A, Double, Mass>
    makeCatchFromSetAccumulator(
        final FishState fishState,
        final String baseName,
        final Supplier<Accumulator<Double>> accumulatorSupplier
    ) {
        return perSpeciesPerRegionMonitor(
            baseName,
            EVERY_YEAR,
            accumulatorSupplier,
            Units.KILOGRAM,
            "Biomass",
            fishState.getSpecies(),
            species -> region -> action ->
                action.getCatchesKept()
                    .map(catchesKept -> catchesKept.getWeightCaught(species))
                    .orElse(0.0),
            regionalDivision
        );
    }

    public Iterable<Monitor<?, ?, ?>> getMonitors() {
        return concat(
            fadDeploymentMonitors,
            allSetsMonitors,
            fadSetMonitors,
            nonAssociatedSetMonitors,
            dolphinSetMonitors,
            ImmutableList.of(biomassLostMonitor),
            otherMonitors
        );
    }

    public Collection<Monitor<AbstractSetAction, ?, ?>> grabAllSetsMonitors() {
        return allSetsMonitors;
    }

    public Collection<Monitor<FadDeploymentAction, ?, ?>> grabFadDeploymentMonitors() {
        return fadDeploymentMonitors;
    }

    public Collection<Monitor<AbstractFadSetAction, ?, ?>> grabFadSetMonitors() {
        return fadSetMonitors;
    }

    public Collection<Monitor<NonAssociatedSetAction, ?, ?>> grabNonAssociatedSetMonitors() {
        return nonAssociatedSetMonitors;
    }

    public Collection<Monitor<DolphinSetAction, ?, ?>> grabDolphinSetMonitors() {
        return dolphinSetMonitors;
    }

    public GroupingMonitor<Species, BiomassLostEvent, Double, Mass> grabBiomassLostMonitor() {
        return biomassLostMonitor;
    }

}
