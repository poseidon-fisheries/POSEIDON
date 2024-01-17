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

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.*;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.*;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Mass;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.concat;
import static java.util.function.Function.identity;
import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.DAY;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy.EVERY_YEAR;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.*;

@SuppressWarnings("rawtypes")
public class DefaultEpoMonitors {

    private final RegionalDivision regionalDivision;
    private final Collection<Monitor<FadDeploymentAction, ?, ?>> fadDeploymentMonitors;
    private final Collection<Monitor<AbstractSetAction, ?, ?>> allSetsMonitors;
    private final Collection<Monitor<AbstractFadSetAction, ?, ?>> fadSetMonitors;
    private final Collection<Monitor<NonAssociatedSetAction, ?, ?>> nonAssociatedSetMonitors;
    private final Collection<Monitor<DolphinSetAction, ?, ?>> dolphinSetMonitors;
    private final GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private final Collection<Monitor<?, ?, ?>> otherMonitors;

    public DefaultEpoMonitors(final FishState fishState) {

        regionalDivision = EpoScenario.REGIONAL_DIVISION;

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
                ONE,
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
        // noinspection unchecked
        final GroupingMonitor<Species, AbstractFadSetAction, Double, Mass>
            catchFromFadSetsMonitor = perSpeciesMonitor(
            "catches from FAD sets",
            EVERY_YEAR,
            SummingAccumulator::new,
            KILOGRAM,
            "Biomass",
            fishState.getSpecies(),
            species -> basicGroupingMonitor(
                species + " catches from FAD sets",
                EVERY_YEAR,
                SummingAccumulator::new,
                KILOGRAM,
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
                ONE,
                "Proportion of sets",
                makeProportionOfSetsOnOwnFadsMonitor
            ),
            new BasicMonitor<>(
                "FAD soak time",
                EVERY_YEAR,
                IterativeAveragingAccumulator::new,
                DAY,
                "Soak time",
                fadSet -> fadSet.getStep() - fadSet.getFad().getStepDeployed()
            ),
            GroupingMonitor.basicPerRegionMonitor(
                "sets on FADs deployed during current trip",
                EVERY_YEAR,
                regionalDivision,
                region -> fadSet -> fadSet.getFisher() != null &&
                    fadSet.getFisher().getCurrentTrip() == fadSet.getFad().getTripDeployed(),
                ProportionAccumulator::new,
                ONE,
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

        biomassLostMonitor = basicPerSpeciesMonitor(
            "biomass lost",
            EVERY_YEAR,
            SummingAccumulator::new,
            KILOGRAM,
            "Biomass",
            fishState.getSpecies(),
            species -> event -> event.getBiomassLost().get(species)
        );

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
                    ONE,
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
                    KILOGRAM,
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
                    DAY,
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
            ONE,
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
            KILOGRAM,
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
