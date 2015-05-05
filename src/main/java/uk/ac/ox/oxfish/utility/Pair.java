package uk.ac.ox.oxfish.utility;

/**
 * The simplest pair object
 * Created by carrknight on 5/4/15.
 */
public class Pair<A,B> {

    final private A first;

    final private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
