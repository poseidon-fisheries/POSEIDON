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

package uk.ac.ox.oxfish.fisher.equipment.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import ec.util.MersenneTwisterFast;
import org.apache.commons.collections15.set.ListOrderedSet;
import sim.util.Bag;
import sim.util.Double2D;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.DriftingPath;
import uk.ac.ox.oxfish.geography.fads.DriftingObjectsMap;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.YearlyActionLimitRegulation;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

public class FadManager {

    private final FadMap fadMap;
    private final ListOrderedSet<Fad> deployedFads = new ListOrderedSet<>();
    private final FadInitializer dudInitializer;
    final private double dudProbability;
    private ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> actionSpecificRegulations;
    private FadInitializer fadInitializer;
    private Fisher fisher;
    private int numFadsInStock;

    public FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock,
        double dudProbability,
        Stream<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this(fadMap, fadInitializer, numFadsInStock, dudProbability, makeRegulationMultimap(actionSpecificRegulations));
    }

    public FadManager(
        FadMap fadMap,
        FadInitializer fadInitializer,
        int numFadsInStock,
        double dudProbability,
        ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this.fadInitializer = fadInitializer;
        this.actionSpecificRegulations = actionSpecificRegulations;
        HashMap<Species, Double> duds = new HashMap<>();
        HashMap<Species, Quantity<Mass>> dudsWeight = new HashMap<>();
        for (Species species : fadInitializer.getBiology().getSpecies()) {
            duds.put(species, 0d);
            dudsWeight.put(species, Quantities.getQuantity(0, Units.KILOGRAM));
        }
        this.dudInitializer = new FadInitializer(
            fadInitializer.getBiology(),
            ImmutableMap.copyOf(dudsWeight),
            ImmutableMap.copyOf(duds),
            0d
        );
        this.dudProbability = dudProbability;
        checkArgument(numFadsInStock >= 0);
        this.numFadsInStock = numFadsInStock;
        this.fadMap = fadMap;
    }

    private static ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> makeRegulationMultimap(
        Stream<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        return actionSpecificRegulations
            .collect(flatteningToImmutableSetMultimap(identity(), reg -> reg.getApplicableActions().stream()))
            .inverse();
    }

    ListOrderedSet<Fad> getDeployedFads() { return deployedFads; }

    public int getNumDeployedFads() { return deployedFads.size(); }

    Optional<Fad> oneOfFadsHere() {
        final Object o = oneOf(getFadsHere(), fisher.grabRandomizer());
        return o instanceof Fad ? Optional.of((Fad) o) : Optional.empty();
    }

    Bag getFadsHere() {
        checkNotNull(fisher);
        return fadMap.fadsAt(fisher.getLocation());
    }

    public int getNumFadsInStock() { return numFadsInStock; }

    public void loseFad(Fad fad) {
        checkArgument(deployedFads.contains(fad));
        deployedFads.remove(fad);
    }

    public Fad deployFad(SeaTile seaTile, int timeStep) {
        final Fad newFad = initFad();
        fadMap.deployFad(newFad, timeStep, seaTile);
        return newFad;
    }

    private Fad initFad() {
        checkState(numFadsInStock >= 1);
        numFadsInStock--;
        final Fad newFad = fisher.grabRandomizer().nextBoolean(dudProbability)
            ? dudInitializer.apply(this)
            : fadInitializer.apply(this);
        deployedFads.add(newFad);
        return newFad;
    }

    /**
     * Deploys a FAD at a random position in the given sea tile
     */
    public void deployFad(SeaTile seaTile, int timeStep, MersenneTwisterFast random) {
        deployFad(new Double2D(
            seaTile.getGridX() + random.nextDouble(),
            seaTile.getGridY() + random.nextDouble()
        ), timeStep);
    }

    private void deployFad(Double2D location, int timeStep) {
        final Fad newFad = initFad();
        fadMap.deployFad(newFad, timeStep, location);
    }

    public void pickUpFad(Fad fad) {
        fadMap.remove(fad);
        numFadsInStock++;
    }

    public FadMap getFadMap() { return fadMap; }

    public ImmutableSetMultimap<SeaTile, Fad> deployedFadsByTileAtStep(int timeStep) {
        final ImmutableSetMultimap.Builder<SeaTile, Fad> builder = new ImmutableSetMultimap.Builder<>();
        final DriftingObjectsMap driftingObjectsMap = fadMap.getDriftingObjectsMap();
        deployedFads.forEach(fad ->
            driftingObjectsMap.getObjectPath(fad)
                .position(timeStep)
                .map(this::getSeaTile)
                .ifPresent(seaTile -> builder.put(seaTile, fad))
        );
        return builder.build();
    }

    private SeaTile getSeaTile(Double2D position) { return getSeaTile(position.x, position.y); }

    private SeaTile getSeaTile(double x, double y) { return getSeaTile((int) x, (int) y); }

    private SeaTile getSeaTile(int x, int y) { return fadMap.getNauticalMap().getSeaTile(x, y);}

    public ImmutableSet<SeaTile> fadLocationsInTimeStepRange(int startStep, int endStep) {
        ImmutableSet.Builder<SeaTile> builder = new ImmutableSet.Builder<>();
        final DriftingObjectsMap driftingObjectsMap = fadMap.getDriftingObjectsMap();
        deployedFads.forEach(fad -> {
            final DriftingPath path = driftingObjectsMap.getObjectPath(fad);
            for (int t = startStep; t <= endStep; t++) {
                path.position(t).map(this::getSeaTile).ifPresent(builder::add);
            }
        });
        return builder.build();
    }

    public boolean isAllowed(FadAction fadAction) {
        return regulationStream(fadAction).allMatch(reg -> reg.isAllowed(fadAction));
    }

    private Stream<ActionSpecificRegulation> regulationStream(FadAction fadAction) {
        return actionSpecificRegulations.get(fadAction.getClass()).stream();
    }

    public void reactToAction(FadAction fadAction) {
        regulationStream(fadAction).forEach(reg -> reg.reactToAction(fadAction));
    }

    public boolean anyYearlyLimitedActionRemaining() {
        final ImmutableList<YearlyActionLimitRegulation> yearlyActionLimitRegulations = getYearlyActionLimitRegulations();
        return yearlyActionLimitRegulations.isEmpty() ||
            yearlyActionLimitRegulations.stream().anyMatch(reg ->
                reg.getNumRemainingActions(getFisher()) > 0
            );
    }

    public ImmutableList<YearlyActionLimitRegulation> getYearlyActionLimitRegulations() {
        return getActionSpecificRegulations()
            .values()
            .stream()
            .filter(reg -> reg instanceof YearlyActionLimitRegulation)
            .map(reg -> (YearlyActionLimitRegulation) reg)
            .collect(toImmutableList());
    }

    public Fisher getFisher() { return fisher; }

    public ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> getActionSpecificRegulations() {
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> actionSpecificRegulations) {
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(Stream<ActionSpecificRegulation> actionSpecificRegulations) {
        setActionSpecificRegulations(makeRegulationMultimap(actionSpecificRegulations));
    }

    public void setFisher(Fisher fisher) {
        this.fisher = fisher;
    }
}
