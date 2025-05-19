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
import uk.ac.ox.oxfish.biology.ConstantLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.NauticalMapFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Consumer;

/**
 * Creates fixed local biologies that are much richer on the left side of the map than on the right
 * Created by carrknight on 6/22/15.
 */
public class FromLeftToRightInitializer extends AbstractBiologyInitializer {


    private final double exponent;
    /**
     * leftmost biomass
     */
    private double maximumBiomass;

    /**
     * how many times we attempt to smooth the biology between two elements
     */
    private int biologySmoothingIndex;


    public FromLeftToRightInitializer(double maximumBiomass, int biologySmoothingIndex, final double exponent) {
        this.maximumBiomass = maximumBiomass;
        this.biologySmoothingIndex = biologySmoothingIndex;
        this.exponent = exponent;
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
        else
            return new ConstantLocalBiology(maximumBiomass *
                Math.pow((1 - seaTile.getGridX() / (double) mapWidthInCells)
                    , exponent));
    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  the randomizer
     * @param model
     */
    @Override
    public void processMap(
        GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model
    ) {

        //standard neighboring tiles smoother
        //a bad copy of the NETLOGO prototype
        final Consumer<NauticalMap> smoother =
            NauticalMapFactory.smoothConstantBiology(
                biologySmoothingIndex, map.getWidth(), map.getHeight()).apply(random);

        smoother.accept(map);


    }

    public double getMaximumBiomass() {
        return maximumBiomass;
    }

    public void setMaximumBiomass(double maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public int getBiologySmoothingIndex() {
        return biologySmoothingIndex;
    }

    public void setBiologySmoothingIndex(int biologySmoothingIndex) {
        this.biologySmoothingIndex = biologySmoothingIndex;
    }

    /**
     * Singleton array with "species 0" in it
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0"};
    }

}
