package uk.ac.ox.oxfish.utility;

/**
 * Created by carrknight on 10/6/16.
 */
public class MutablePair<A,B> {


    private A first;

    private B second;

    public MutablePair(A first, B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    /**
     * Setter for property 'first'.
     *
     * @param first Value to set for property 'first'.
     */
    public void setFirst(A first) {
        this.first = first;
    }

    /**
     * Setter for property 'second'.
     *
     * @param second Value to set for property 'second'.
     */
    public void setSecond(B second) {
        this.second = second;
    }
}
