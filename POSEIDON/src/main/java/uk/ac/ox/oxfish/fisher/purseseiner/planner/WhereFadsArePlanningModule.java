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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

/**
 * simply picks the best fad in the area where there are most fads, weighted by the age of the fad:
 * <p>
 * (best area has max SUM(FAD_AGE^AGE_WEIGHT)
 */
public class WhereFadsArePlanningModule extends PickBestPilePlanningModule {


    private final double ageWeight;


    public WhereFadsArePlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator,
        final double ageWeight
    ) {

        super(optionsGenerator);
        this.ageWeight = ageWeight;


    }


    @Override
    protected double weighFad(
        final int currentModelStep,
        final OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad
    ) {
        //get the age of the fad
        final int age = valuedFad.getKey().isActive() ?
            currentModelStep - valuedFad.getKey().getStepDeployed() :
            0;
        //age ^ weight
        return Math.pow(age, ageWeight);
    }


}
