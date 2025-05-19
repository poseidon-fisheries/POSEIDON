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

package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Predicate;

/**
 * Created by carrknight on 10/4/16.
 */
public abstract class AbstractAdaptation<T> implements Adaptation {


    /**
     * used by the agent to tell what is its current T object and what is the T of
     * others
     */
    private Sensor<Fisher, T> sensor;

    /**
     * a class that assigns a new T to the fisher
     */
    private Actuator<Fisher, T> actuator;

    /**
     * each "step" the validator makes sure the fisher is ready to adapt; if it returns false the adaptation is aborted
     */
    private Predicate<Fisher> validator;


    private FishState model;


    public AbstractAdaptation(
        final Sensor<Fisher, T> sensor, final Actuator<Fisher, T> actuator, final Predicate<Fisher> validator
    ) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.validator = validator;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.model = model;
        onStart(model, fisher);
    }

    protected abstract void onStart(FishState model, Fisher fisher);

    /**
     * Ask yourself to adapt
     *
     * @param toAdapt who is doing the adaptation
     * @param state
     * @param random  the randomizer
     */
    @Override
    public void adapt(final Fisher toAdapt, final FishState state, final MersenneTwisterFast random) {
        //are you ready?
        if (!validator.test(toAdapt))
            return;

        final T newVariable = concreteAdaptation(toAdapt, state, random);
        if (newVariable != null && newVariable != sensor.scan(toAdapt))
            actuator.apply(toAdapt, newVariable, state);


    }


    abstract public T concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random);


    /**
     * Getter for property 'sensor'.
     *
     * @return Value for property 'sensor'.
     */
    public Sensor<Fisher, T> getSensor() {
        return sensor;
    }

    /**
     * Setter for property 'sensor'.
     *
     * @param sensor Value to set for property 'sensor'.
     */
    public void setSensor(final Sensor<Fisher, T> sensor) {
        this.sensor = sensor;
    }

    /**
     * Getter for property 'actuator'.
     *
     * @return Value for property 'actuator'.
     */
    public Actuator<Fisher, T> getActuator() {
        return actuator;
    }

    /**
     * Setter for property 'actuator'.
     *
     * @param actuator Value to set for property 'actuator'.
     */
    public void setActuator(final Actuator<Fisher, T> actuator) {
        this.actuator = actuator;
    }

    /**
     * Getter for property 'validator'.
     *
     * @return Value for property 'validator'.
     */
    public Predicate<Fisher> getValidator() {
        return validator;
    }

    /**
     * Setter for property 'validator'.
     *
     * @param validator Value to set for property 'validator'.
     */
    public void setValidator(final Predicate<Fisher> validator) {
        this.validator = validator;
    }
}
