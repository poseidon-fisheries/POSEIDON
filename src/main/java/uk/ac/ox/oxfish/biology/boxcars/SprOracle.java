/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

/**
 * has access to the population and computes SPR that way, even though it simplifies maturity to jack-knife
 */
public class SprOracle implements AdditionalStartable {


    private final Species species;

    private final double maturityLength;

    private final int dayOfMeasurement;

    private double spr = Double.NaN;

    /**
     * the Spawning biomass (in kg) for virgin fish
     */
    private final double virginSpawningBiomass;
    private int spawningSubdivision;

    public SprOracle(Species species, double maturityLength, int dayOfMeasurement, double virginSpawningBiomass) {
        this.species = species;
        this.maturityLength = maturityLength;
        this.dayOfMeasurement = dayOfMeasurement;
        this.virginSpawningBiomass = virginSpawningBiomass;
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        model.scheduleEveryYear(new Steppable() {
            @Override
            public void step(SimState simState) {
                model.scheduleOnceInXDays(new Steppable() {
                    @Override
                    public void step(SimState simState) {

                        double spawningBiomass = 0;
                        for(int i=0; i<species.getNumberOfBins(); i++) {
                            spawningSubdivision = 0;
                            if(species.getLength(spawningSubdivision, i)>maturityLength)
                                spawningBiomass += model.getTotalAbundance(species,i) *
                                        species.getWeight(spawningSubdivision,i);
                        }

                        spr = spawningBiomass/virginSpawningBiomass;
                    }
                },StepOrder.YEARLY_DATA_GATHERING,dayOfMeasurement);
            }
        }, StepOrder.DAWN);

        model.getYearlyDataSet().registerGatherer(
                "SPR Oracle - " + species,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {

                        return spr;
                    }
                },Double.NaN
        );

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}
