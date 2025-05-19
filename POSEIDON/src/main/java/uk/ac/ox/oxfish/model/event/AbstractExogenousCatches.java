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

package uk.ac.ox.oxfish.model.event;

import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;

import java.util.LinkedHashMap;
import java.util.List;

import static tech.units.indriya.unit.Units.KILOGRAM;

/**
 * an abstract class that deals with data gatherrs and other basic utilities of the exogenous catch
 * but doesn't actually do any "catching" except scheduling itself every year
 */
public abstract class AbstractExogenousCatches implements ExogenousCatches {


    private static final long serialVersionUID = 3886767741185374010L;
    protected final LinkedHashMap<Species, Double> lastExogenousCatchesMade = new LinkedHashMap<>();
    private final String columnName;
    private Stoppable stoppable;

    public AbstractExogenousCatches(
        final String dataColumnName
    ) {
        columnName = dataColumnName;
    }


    protected List<? extends LocalBiology> getAllCatchableBiologies(final FishState model) {
        return model.getMap().getAllSeaTilesExcludingLandAsList();
    }

    protected Double getFishableBiomass(final Species target, final LocalBiology seaTile) {

        return seaTile.getBiomass(target);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {

        //schedule yourself at the end of the year!
        model.scheduleOnceInXDays((Steppable) simState -> {
            AbstractExogenousCatches.this.step(model);
            stoppable = model.scheduleEveryYear(
                AbstractExogenousCatches.this,
                StepOrder.BIOLOGY_PHASE
            );
        }, StepOrder.BIOLOGY_PHASE, 364);


        for (final Species species : model.getSpecies()) {
            model.getYearlyDataSet().registerGatherer(
                columnName + species,
                (Gatherer<FishState>) state -> lastExogenousCatchesMade.get(species),
                0,
                KILOGRAM,
                "Biomass"
            );
        }
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if (stoppable != null)
            stoppable.stop();
    }


}
