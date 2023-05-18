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

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * An exogenous local biology object that, when asked for biomass available just calls a previously given function
 * to return the answer. Doesn't track biomass fished.
 * Created by carrknight on 2/6/17.
 */
public class GetterLocalBiology extends AbstractBiomassBasedBiology {


    /**
     * species being fished
     */
    private final Species species;

    /**
     * the function that actually returns the amount of biomass available
     */
    private final Function<FishState, Double> biomass;

    /**
     * the model
     */
    private FishState state;


    public GetterLocalBiology(
        Species species, Function<FishState, Double> biomass
    ) {
        this.species = species;
        this.biomass = biomass;
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
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        this.state = null;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(Species species) {

        if (species == this.species)
            return Math.max(0, biomass.apply(state));
        return
            0d;
    }

    /**
     * ignored
     *
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
        Catch caught, Catch notDiscarded, GlobalBiology biology
    ) {

        //ignored
    }


    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }

    /**
     * Getter for property 'biomass'.
     *
     * @return Value for property 'biomass'.
     */
    public Function<FishState, Double> getBiomassFunction() {
        return biomass;
    }
}
