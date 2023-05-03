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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

/**
 * decorates any local biology by intercepting calls to "reactToxBeingFished" but only up to a certain year
 */
public class UnfishableLocalBiologyDecorator implements LocalBiology {

    private final int yearAfterWhichBiomassStopsBeingProtected;

    /**
     * reference needed to check the time
     */
    private FishState state;

    private final LocalBiology decorated;

    public UnfishableLocalBiologyDecorator(
            int yearAfterWhichBiomassStopsBeingProtected, LocalBiology decorated) {
        this.yearAfterWhichBiomassStopsBeingProtected = yearAfterWhichBiomassStopsBeingProtected;
        this.decorated = decorated;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(Species species) {
        return decorated.getBiomass(species);
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param caught       the amount fished out of the sea
     * @param notDiscarded the amount retained in the boat (not thrown out)
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {

        Preconditions.checkArgument(state!=null, "Not started or already turned off!");
        if(state.getYear()>=yearAfterWhichBiomassStopsBeingProtected)
            decorated.reactToThisAmountOfBiomassBeingFished(caught, notDiscarded, biology);

    }

    @Override
    public StructuredAbundance getAbundance(Species species) {
        return decorated.getAbundance(species);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        this.state = model;
        decorated.start(model);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        this.state = null;
        decorated.turnOff();
    }

    /**
     * Getter for property 'decorated'.
     *
     * @return Value for property 'decorated'.
     */
    public LocalBiology getDecorated() {
        return decorated;
    }
}
