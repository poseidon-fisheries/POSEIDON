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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.monitors.BasicMonitor;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.Monitor;
import uk.ac.ox.oxfish.model.data.monitors.ObservingAtIntervalMonitor;
import uk.ac.ox.oxfish.model.data.monitors.ProportionalGatherer;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.Accumulator;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.IncrementingAccumulator;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.IterativeAveragingAccumulator;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.ProportionAccumulator;
import uk.ac.ox.oxfish.model.data.monitors.accumulators.SummingAccumulator;
import uk.ac.ox.oxfish.model.data.monitors.regions.RegionalDivision;
import uk.ac.ox.oxfish.model.data.monitors.regions.TicTacToeRegionalDivision;

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
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.basicGroupingMonitor;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.basicPerRegionMonitor;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.basicPerSpeciesMonitor;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.perRegionMonitor;
import static uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor.perSpeciesPerRegionMonitor;

public class Monitors {

    private final RegionalDivision regionalDivision;
    private final Collection<Monitor<FadDeploymentAction, ?, ?>> fadDeploymentMonitors;
    private final Collection<Monitor<AbstractFadSetAction, ?, ?>> fadSetMonitors;
    private final Collection<Monitor<NonAssociatedSetAction, ?, ?>> unassociatedSetMonitors;
    private final GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private final Collection<Monitor<?, ?, ?>> otherMonitors;

    public Monitors(FishState fishState) {

        regionalDivision = new TicTacToeRegionalDivision(fishState.getMap());
        final FishStateYearlyTimeSeries yearlyTimeSeries = fishState.getYearlyDataSet();

        fadDeploymentMonitors = ImmutableList.of(
            makeActionCounter("FAD deployments")
        );

        final Function<RegionalDivision.Region, ProportionalGatherer<Boolean, AbstractFadSetAction, AbstractFadSetAction, Dimensionless>>
            makeProportionOfSetsOnOwnFadsMonitor =
            region -> new ProportionalGatherer<>(basicGroupingMonitor(
                null, // we already have the total number of sets and don't want to gather it from here
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
        fadSetMonitors = ImmutableList.of(
            makeActionCounter("FAD sets"),
            makeCatchFromSetAccumulator(
                fishState, "catches from FAD sets", SummingAccumulator::new),
            makeCatchFromSetAccumulator(
                fishState, "catches by FAD sets", IterativeAveragingAccumulator::new),
            makeProportionOfSetsOnOwnFadsMonitor.apply(null),
            perRegionMonitor(
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
            basicPerRegionMonitor(
                "sets on FADs deployed during current trip",
                EVERY_YEAR,
                regionalDivision,
                region -> fadSet -> fadSet.getFisher().getCurrentTrip() == fadSet.getFad().getTripDeployed(),
                ProportionAccumulator::new,
                ONE,
                "Proportion of sets"
            )
        );

        unassociatedSetMonitors = ImmutableList.of(
            makeActionCounter("unassociated sets"),
            makeCatchFromSetAccumulator(
                fishState, "catches from unassociated sets", SummingAccumulator::new),
            makeCatchFromSetAccumulator(
                fishState, "catches by unassociated sets", IterativeAveragingAccumulator::new)
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
            )
        );

        getMonitors().forEach(monitor -> monitor.registerWith(yearlyTimeSeries));

    }

    private <E extends PurseSeinerAction> ProportionalGatherer<RegionalDivision.Region, E, E, Dimensionless> makeActionCounter(
        String actionName
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

    private <A extends AbstractSetAction> GroupingMonitor<Species, A, Double, Mass> makeCatchFromSetAccumulator(
        FishState fishState,
        String baseName,
        Supplier<Accumulator<Double>> accumulatorSupplier
    ) {
        return perSpeciesPerRegionMonitor(
            baseName,
            EVERY_YEAR,
            accumulatorSupplier,
            KILOGRAM,
            "Biomass",
            fishState.getSpecies(),
            species -> region -> action -> action.getCatchesKept()
                .map(catchesKept -> catchesKept.getWeightCaught(species))
                .orElse(0.0),
            regionalDivision
        );
    }

    public Iterable<Monitor<?, ?, ?>> getMonitors() {
        return concat(
            fadDeploymentMonitors,
            fadSetMonitors,
            unassociatedSetMonitors,
            ImmutableList.of(biomassLostMonitor),
            otherMonitors
        );
    }

    public Collection<Monitor<FadDeploymentAction, ?, ?>> getFadDeploymentMonitors() { return fadDeploymentMonitors; }

    public Collection<Monitor<AbstractFadSetAction, ?, ?>> getFadSetMonitors() { return fadSetMonitors; }

    public Collection<Monitor<NonAssociatedSetAction, ?, ?>> getUnassociatedSetMonitors() { return unassociatedSetMonitors; }

    public GroupingMonitor<Species, BiomassLostEvent, Double, Mass> getBiomassLostMonitor() { return biomassLostMonitor; }

    public Collection<Monitor<?, ?, ?>> getOtherMonitors() { return otherMonitors; }

}
