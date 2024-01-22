/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology;

import com.google.common.collect.EvictingQueue;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This is not the full Deriso-Schnute model, rather it's just the initial equation given here:
 * https://catalyst.uw.edu/workspace/file/download/d75d78b22acb18071f8ddc321cda7ed08a74f39da15311374e3c10e448045bc7
 * <p>
 * Created by carrknight on 10/19/15.
 */
public class SingleSpecieLogisticDelayGrowthBiology extends AbstractBiomassBasedBiology
    implements Steppable, Startable {

    private static final long serialVersionUID = -7464424618406733266L;
    /**
     * how many years before current spawns enter the biomass proper
     */
    private final int yearDelays;

    /**
     * storage of all the past biomasses (for recruitment purposes)
     */
    private final EvictingQueue<Double> pastBiomass;

    /**
     * numerator parameter in the recruitment equation
     */
    final private double aParameter;

    /**
     * denominator parameter in the recruitment equation
     */
    final private double bParameter;
    /**
     * biomass over which you can never go
     */
    final private double maxBiomass;
    /**
     * the species modeled
     */
    private final Species species;
    /**
     * current biomass
     */
    private double currentBiomass;
    private Stoppable stoppable;

    public SingleSpecieLogisticDelayGrowthBiology(
        final Species species,
        final double currentBiomass,
        final double maxBiomass,
        final int yearDelays,
        final double aParameter,
        final double bParameter
    ) {
        checkArgument(yearDelays > 0, "Use undelayed biology rather than feeding 0 to a delayed one");
        checkArgument(maxBiomass > 0);
        checkArgument(currentBiomass <= maxBiomass);
        checkArgument(currentBiomass >= 0);
        this.species = species;
        this.yearDelays = yearDelays;
        pastBiomass = EvictingQueue.create(yearDelays);
        while (pastBiomass.remainingCapacity() > 0)
            pastBiomass.add(currentBiomass);
        this.aParameter = aParameter;
        this.bParameter = bParameter;
        this.currentBiomass = currentBiomass;
        this.maxBiomass = maxBiomass;
    }

    @Override
    public double getTotalBiomass() {
        return getBiomass(this.species);
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(final Species species) {
        checkArgument(species == this.species, "%s != %s", species, this.species);
        return currentBiomass;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
        final Catch caught,
        final Catch notDiscarded,
        final GlobalBiology biology
    ) {
        // focus on only the one you care about!
        final double biomassFished = caught.getWeightCaught(this.species);

        checkArgument(biomassFished <= currentBiomass);
        currentBiomass -= biomassFished;

    }

    @Override
    public void step(final SimState simState) {

        final double biologyAtRecruitment = pastBiomass.poll();
        assert pastBiomass.remainingCapacity() == 1;
        final double recruitment = aParameter * biologyAtRecruitment / (bParameter + biologyAtRecruitment);

        currentBiomass = Math.min(currentBiomass + recruitment, maxBiomass);
        pastBiomass.add(currentBiomass);

    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables or just
     * to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        stoppable = model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        stoppable.stop();
    }

    public double getaParameter() {
        return aParameter;
    }

    public double getbParameter() {
        return bParameter;
    }

    public int getYearDelays() {
        return yearDelays;
    }
}
