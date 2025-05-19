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

package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.LinearGetterBiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 2/6/17.
 */
public class LinearGetterBiologyFactory implements AlgorithmFactory<LinearGetterBiologyInitializer> {


    private DoubleParameter intercept = new FixedDoubleParameter(1000);

    private DoubleParameter x = new FixedDoubleParameter(0);

    private DoubleParameter y = new FixedDoubleParameter(0);

    private DoubleParameter xDay = new FixedDoubleParameter(0);

    private DoubleParameter yDay = new FixedDoubleParameter(0);

    private DoubleParameter rocky = new FixedDoubleParameter(0);

    private DoubleParameter xRocky = new FixedDoubleParameter(0);

    private DoubleParameter yRocky = new FixedDoubleParameter(0);

    private DoubleParameter dayRocky = new FixedDoubleParameter(0);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LinearGetterBiologyInitializer apply(final FishState state) {
        return new LinearGetterBiologyInitializer(
            intercept.applyAsDouble(state.getRandom()),
            x.applyAsDouble(state.getRandom()),
            y.applyAsDouble(state.getRandom()),
            xDay.applyAsDouble(state.getRandom()),
            yDay.applyAsDouble(state.getRandom()),
            rocky.applyAsDouble(state.getRandom()),
            xRocky.applyAsDouble(state.getRandom()),
            yRocky.applyAsDouble(state.getRandom()),
            dayRocky.applyAsDouble(state.getRandom())
        );
    }

    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public DoubleParameter getIntercept() {
        return intercept;
    }

    /**
     * Setter for property 'intercept'.
     *
     * @param intercept Value to set for property 'intercept'.
     */
    public void setIntercept(final DoubleParameter intercept) {
        this.intercept = intercept;
    }

    /**
     * Getter for property 'x'.
     *
     * @return Value for property 'x'.
     */
    public DoubleParameter getX() {
        return x;
    }

    /**
     * Setter for property 'x'.
     *
     * @param x Value to set for property 'x'.
     */
    public void setX(final DoubleParameter x) {
        this.x = x;
    }

    /**
     * Getter for property 'y'.
     *
     * @return Value for property 'y'.
     */
    public DoubleParameter getY() {
        return y;
    }

    /**
     * Setter for property 'y'.
     *
     * @param y Value to set for property 'y'.
     */
    public void setY(final DoubleParameter y) {
        this.y = y;
    }

    /**
     * Getter for property 'xDay'.
     *
     * @return Value for property 'xDay'.
     */
    public DoubleParameter getxDay() {
        return xDay;
    }

    /**
     * Setter for property 'xDay'.
     *
     * @param xDay Value to set for property 'xDay'.
     */
    public void setxDay(final DoubleParameter xDay) {
        this.xDay = xDay;
    }

    /**
     * Getter for property 'yDay'.
     *
     * @return Value for property 'yDay'.
     */
    public DoubleParameter getyDay() {
        return yDay;
    }

    /**
     * Setter for property 'yDay'.
     *
     * @param yDay Value to set for property 'yDay'.
     */
    public void setyDay(final DoubleParameter yDay) {
        this.yDay = yDay;
    }

    /**
     * Getter for property 'rocky'.
     *
     * @return Value for property 'rocky'.
     */
    public DoubleParameter getRocky() {
        return rocky;
    }

    /**
     * Setter for property 'rocky'.
     *
     * @param rocky Value to set for property 'rocky'.
     */
    public void setRocky(final DoubleParameter rocky) {
        this.rocky = rocky;
    }

    /**
     * Getter for property 'xRocky'.
     *
     * @return Value for property 'xRocky'.
     */
    public DoubleParameter getxRocky() {
        return xRocky;
    }

    /**
     * Setter for property 'xRocky'.
     *
     * @param xRocky Value to set for property 'xRocky'.
     */
    public void setxRocky(final DoubleParameter xRocky) {
        this.xRocky = xRocky;
    }

    /**
     * Getter for property 'yRocky'.
     *
     * @return Value for property 'yRocky'.
     */
    public DoubleParameter getyRocky() {
        return yRocky;
    }

    /**
     * Setter for property 'yRocky'.
     *
     * @param yRocky Value to set for property 'yRocky'.
     */
    public void setyRocky(final DoubleParameter yRocky) {
        this.yRocky = yRocky;
    }

    /**
     * Getter for property 'dayRocky'.
     *
     * @return Value for property 'dayRocky'.
     */
    public DoubleParameter getDayRocky() {
        return dayRocky;
    }

    /**
     * Setter for property 'dayRocky'.
     *
     * @param dayRocky Value to set for property 'dayRocky'.
     */
    public void setDayRocky(final DoubleParameter dayRocky) {
        this.dayRocky = dayRocky;
    }
}
