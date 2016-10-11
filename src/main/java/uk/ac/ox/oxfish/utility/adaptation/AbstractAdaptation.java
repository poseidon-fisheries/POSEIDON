package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Predicate;

/**
 * Created by carrknight on 10/4/16.
 */
public abstract class AbstractAdaptation<T> implements  Adaptation<T>{


    /**
     * used by the agent to tell what is its current T object and what is the T of
     * others
     */
    private Sensor<Fisher,T> sensor;

    /**
     * a class that assigns a new T to the fisher
     */
    private Actuator<Fisher,T> actuator;

    /**
     * each "step" the validator makes sure the fisher is ready to adapt; if it returns false the adaptation is aborted
     */
    private Predicate<Fisher> validator;


    private FishState model;


    @Override
    public void start(FishState model, Fisher fisher) {
        this.model=model;
        onStart(model,fisher);
    }

    protected abstract void onStart(FishState model, Fisher fisher);

    public AbstractAdaptation(
            Sensor<Fisher,T> sensor, Actuator<Fisher,T> actuator, Predicate<Fisher> validator) {
        this.sensor = sensor;
        this.actuator = actuator;
        this.validator = validator;
    }


    /**
     * Ask yourself to adapt
     *  @param toAdapt who is doing the adaptation
     * @param state
     * @param random  the randomizer
     */
    @Override
    public void adapt(Fisher toAdapt, FishState state, MersenneTwisterFast random) {
        //are you ready?
        if(!validator.test(toAdapt))
            return;

        T newVariable = concreteAdaptation(toAdapt,state,random);
        if(newVariable!= null && newVariable != sensor.scan(toAdapt))
            actuator.apply(toAdapt,newVariable,state );


    }


    abstract public T concreteAdaptation(Fisher toAdapt, FishState state, MersenneTwisterFast random);


    /**
     * Getter for property 'sensor'.
     *
     * @return Value for property 'sensor'.
     */
    public Sensor<Fisher,T> getSensor() {
        return sensor;
    }

    /**
     * Setter for property 'sensor'.
     *
     * @param sensor Value to set for property 'sensor'.
     */
    public void setSensor(Sensor<Fisher,T> sensor) {
        this.sensor = sensor;
    }

    /**
     * Getter for property 'actuator'.
     *
     * @return Value for property 'actuator'.
     */
    public Actuator<Fisher,T> getActuator() {
        return actuator;
    }

    /**
     * Setter for property 'actuator'.
     *
     * @param actuator Value to set for property 'actuator'.
     */
    public void setActuator(Actuator<Fisher,T> actuator) {
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
    public void setValidator(Predicate<Fisher> validator) {
        this.validator = validator;
    }
}
