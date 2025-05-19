/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.ConstantHeterogeneousLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * From left to right, two species, well-mixed
 * Created by carrknight on 6/20/16.
 */
public class FromLeftToRightMixedInitializer extends AbstractBiologyInitializer {

    final private double proportionSecondSpeciesToFirst;

    /**
     * leftmost biomass
     */
    final private double maximumBiomass;


    private String firstSpeciesName = "Species 0";


    private String secondSpeciesName = "Species 1";


    public FromLeftToRightMixedInitializer(
        double maximumBiomass,
        double proportionSecondSpeciesToFirst
    ) {
        this.proportionSecondSpeciesToFirst = proportionSecondSpeciesToFirst;
        this.maximumBiomass = maximumBiomass;
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
        GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
        int mapWidthInCells, NauticalMap map
    ) {
        if (seaTile.isLand())
            return new EmptyLocalBiology();
        else {
            double correctBiomass = maximumBiomass *
                Math.pow((1 - seaTile.getGridX() / (double) mapWidthInCells)
                    , 2);

            return new ConstantHeterogeneousLocalBiology(
                correctBiomass,
                correctBiomass *
                    proportionSecondSpeciesToFirst
            );
        }
    }


    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
        GlobalBiology biology,
        NauticalMap map,
        MersenneTwisterFast random,
        FishState model
    ) {

    }

    /**
     * "Species 0" and "Species 1"
     *
     * @return the name of the species
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{
            firstSpeciesName,
            secondSpeciesName};
    }


    public String getFirstSpeciesName() {
        return firstSpeciesName;
    }

    public void setFirstSpeciesName(String firstSpeciesName) {
        this.firstSpeciesName = firstSpeciesName;
    }

    public String getSecondSpeciesName() {
        return secondSpeciesName;
    }

    public void setSecondSpeciesName(String secondSpeciesName) {
        this.secondSpeciesName = secondSpeciesName;
    }

    /**
     * Getter for property 'proportionSecondSpeciesToFirst'.
     *
     * @return Value for property 'proportionSecondSpeciesToFirst'.
     */
    public double getProportionSecondSpeciesToFirst() {
        return proportionSecondSpeciesToFirst;
    }

    /**
     * Getter for property 'maximumBiomass'.
     *
     * @return Value for property 'maximumBiomass'.
     */
    public double getMaximumBiomass() {
        return maximumBiomass;
    }
}
