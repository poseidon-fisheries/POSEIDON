/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.boxcars;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.LinkedList;
import java.util.List;

/**
 * collects the "real" biological count of how many fish for each bin are there in the model
 */
public class AbundanceGatherers implements AdditionalStartable {

    private final int dayOfMeasurement;
    /**
     * hanndy list of all the columns we create
     */
    private final List<String> columnsCreated;
    private double[][][] abundancePerSpecies;
    private Stoppable stoppable;


    public AbundanceGatherers(int dayOfMeasurement) {
        this.dayOfMeasurement = dayOfMeasurement;
        columnsCreated = new LinkedList<>();
    }


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {


        abundancePerSpecies = new double[model.getBiology().getSize()][][];
        for (Species species : model.getSpecies()) {
            abundancePerSpecies[species.getIndex()] = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
                for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                    int finalSubdivision = subdivision;
                    int finalBin = bin;
                    String columnName = species + " " + "Abundance " + subdivision + "." + bin + " at day " + dayOfMeasurement;
                    model.getYearlyDataSet().registerGatherer(
                        columnName,
                        (Gatherer<FishState>) fishState -> abundancePerSpecies[species.getIndex()][finalSubdivision][finalBin], Double.NaN
                    );
                    columnsCreated.add(columnName);
                }

            }


        }


        //first step
        model.scheduleOnceInXDays((Steppable) simState -> updateStep(model),
            StepOrder.YEARLY_DATA_GATHERING,
            dayOfMeasurement);

        //other steps
        stoppable = model.scheduleEveryYear((Steppable) simState -> model.scheduleOnceInXDays((Steppable) simState1 -> updateStep(
            model), StepOrder.YEARLY_DATA_GATHERING, dayOfMeasurement), StepOrder.DAWN);


    }

    public void updateStep(FishState model) {
        for (Species species : model.getSpecies()) {
            for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++) {
                for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                    abundancePerSpecies[species.getIndex()][subdivision][bin] =
                        model.getTotalAbundance(species, subdivision, bin);
                }
            }
        }
    }


    public List<String> getColumnsCreated() {
        return columnsCreated;
    }


    @Override
    public void turnOff() {
        abundancePerSpecies = null;
        columnsCreated.clear();
        if (stoppable != null)
            stoppable.stop();
    }
}
