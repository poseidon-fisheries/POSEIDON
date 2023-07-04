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

package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grows biomass in each given cell independently
 * Created by carrknight on 1/31/17.
 */
public class IndependentLogisticBiomassGrower implements Startable, Steppable {


    private static final long serialVersionUID = 2221569851814511962L;
    /**
     * the uninpeded growth rate of each species
     */
    private final double malthusianParameter;
    private final Species species;
    /**
     * list of biologies to grow. You can use a single grower for all the cells or a separate grower
     * for each cell. It shouldn't be too much of a big deal.
     */
    private List<BiomassLocalBiology> biologies = new LinkedList<>();
    private Stoppable receipt;


    public IndependentLogisticBiomassGrower(
        final double malthusianParameter,
        final Species species
    ) {
        this.malthusianParameter = malthusianParameter;
        this.species = species;
    }

    @Override
    public void step(final SimState simState) {

        final FishState model = ((FishState) simState);

        //remove all the biologies that stopped
        biologies = biologies.stream().filter(
            logisticLocalBiology -> !logisticLocalBiology.isStopped()).collect(Collectors.toList());

        //for each place
        for (final VariableBiomassBasedBiology biology : biologies) {
            //grow fish

            final double[] currentBiomasses = biology.getCurrentBiomass();


            final int speciesIndex = species.getIndex();
            assert currentBiomasses[speciesIndex] >= 0;
            //grows logistically

            final double carryingCapacity = biology.getCarryingCapacity(speciesIndex);
            if (carryingCapacity > FishStateUtilities.EPSILON && carryingCapacity > currentBiomasses[speciesIndex]) {
                final double oldBiomass = currentBiomasses[speciesIndex];
                currentBiomasses[speciesIndex] = logisticStep(
                    currentBiomasses[speciesIndex],
                    carryingCapacity,
                    malthusianParameter
                );
                //store recruitment number, counter should have been initialized by factory!
                final double recruitment = currentBiomasses[speciesIndex] - oldBiomass;
                if (recruitment > FishStateUtilities.EPSILON)
                    model.getYearlyCounter().count(
                        model.getSpecies().get(speciesIndex) +
                            " Recruitment",
                        recruitment
                    );

            }
            assert currentBiomasses[speciesIndex] >= 0;

        }


        if (biologies.size() == 0) //if you removed all the biologies then we are done
            turnOff();
    }

    public static double logisticStep(
        final double currentBiomasses, final double carryingCapacity, final double malthusianParameter
    ) {
        return Math.min(
            carryingCapacity,
            currentBiomasses + logisticRecruitment(currentBiomasses, carryingCapacity, malthusianParameter)
        );
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();

    }

    public static double logisticRecruitment(
        final double currentBiomasses,
        final double carryingCapacity,
        final double malthusianParameter
    ) {
        return malthusianParameter *
            (1d - currentBiomasses / carryingCapacity) * currentBiomasses;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        //schedule yourself
        Preconditions.checkArgument(receipt == null, "Already started!");
        receipt = model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * Getter for property 'biologies'.
     *
     * @return Value for property 'biologies'.
     */
    public List<BiomassLocalBiology> getBiologies() {
        return biologies;
    }


    /**
     * Getter for property 'malthusianParameter'.
     *
     * @return Value for property 'malthusianParameter'.
     */
    public double getMalthusianParameter() {
        return malthusianParameter;
    }
}
