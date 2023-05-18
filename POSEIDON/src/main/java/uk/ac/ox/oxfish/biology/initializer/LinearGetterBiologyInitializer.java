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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * Each seatile has biomass driven by a linear function: <br>
 * intercept + x + y + x*t + y*t + rocky + rocky*x + rocky*y + rocky*t
 * Created by carrknight on 2/6/17.
 */
public class LinearGetterBiologyInitializer implements BiologyInitializer {


    private final double intercept;

    private final double x;

    private final double y;

    private final double xDay;

    private final double yDay;

    private final double rocky;

    private final double xRocky;

    private final double yRocky;

    private final double dayRocky;


    public LinearGetterBiologyInitializer(
        double intercept,
        double x,
        double y,
        double xDay,
        double yDay,
        double rocky,
        double xRocky,
        double yRocky,
        double dayRocky
    ) {
        this.intercept = intercept;
        this.x = x;
        this.y = y;
        this.xDay = xDay;
        this.yDay = yDay;
        this.rocky = rocky;
        this.xRocky = xRocky;
        this.yRocky = yRocky;
        this.dayRocky = dayRocky;
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

        Species species = biology.getSpecie(0);
        return new GetterLocalBiology(
            species,
            new Function<FishState, Double>() {
                @Override
                public Double apply(FishState state) {
                    return intercept +
                        seaTile.getGridX() * x +
                        seaTile.getGridY() * y +
                        seaTile.getGridX() * state.getDayOfTheYear() * xDay +
                        seaTile.getGridY() * state.getDayOfTheYear() * yDay +
                        seaTile.getRockyPercentage() * rocky +
                        seaTile.getGridX() * seaTile.getRockyPercentage() * xRocky +
                        seaTile.getGridY() * seaTile.getRockyPercentage() * yRocky +
                        state.getDayOfTheYear() * seaTile.getRockyPercentage() * dayRocky;

                }
            }
        );

    }


    /**
     * ignored
     */
    @Override
    public void processMap(
        GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model
    ) {

    }

    /**
     * Single species biology
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {
        return new GlobalBiology(new Species("Species 0"));
    }

    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public double getIntercept() {
        return intercept;
    }

    /**
     * Getter for property 'x'.
     *
     * @return Value for property 'x'.
     */
    public double getX() {
        return x;
    }

    /**
     * Getter for property 'y'.
     *
     * @return Value for property 'y'.
     */
    public double getY() {
        return y;
    }

    /**
     * Getter for property 'xDay'.
     *
     * @return Value for property 'xDay'.
     */
    public double getxDay() {
        return xDay;
    }

    /**
     * Getter for property 'yDay'.
     *
     * @return Value for property 'yDay'.
     */
    public double getyDay() {
        return yDay;
    }

    /**
     * Getter for property 'rocky'.
     *
     * @return Value for property 'rocky'.
     */
    public double getRocky() {
        return rocky;
    }

    /**
     * Getter for property 'xRocky'.
     *
     * @return Value for property 'xRocky'.
     */
    public double getxRocky() {
        return xRocky;
    }

    /**
     * Getter for property 'yRocky'.
     *
     * @return Value for property 'yRocky'.
     */
    public double getyRocky() {
        return yRocky;
    }

    /**
     * Getter for property 'dayRocky'.
     *
     * @return Value for property 'dayRocky'.
     */
    public double getDayRocky() {
        return dayRocky;
    }
}
