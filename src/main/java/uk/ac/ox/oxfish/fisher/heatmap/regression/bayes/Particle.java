package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Created by carrknight on 7/29/16.
 */
public class Particle<V> {


    private V position;

    private double weight = 1;

    public Particle(V position) {
        this.position = position;
    }

    /**
     * Getter for property 'position'.
     *
     * @return Value for property 'position'.
     */
    public V getPosition() {
        return position;
    }

    /**
     * Getter for property 'weight'.
     *
     * @return Value for property 'weight'.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Setter for property 'weight'.
     *
     * @param weight Value to set for property 'weight'.
     */
    public void setWeight(double weight) {
        this.weight = FishStateUtilities.round5(weight);
    }

    public void setPosition(V position) {
        this.position = position;
    }
}
