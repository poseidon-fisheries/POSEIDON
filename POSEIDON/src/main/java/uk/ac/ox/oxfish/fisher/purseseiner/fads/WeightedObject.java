package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import java.util.Map.Entry;

/**
 * an helper class, really a pair, containing a reference to something with its weight.
 * Better however is to think of it as a weight number with a reference to what is being weighted
 */
public class WeightedObject<KEY> extends Number {

    private final KEY objectBeingWeighted;

    private final double totalWeight;

    public static <K, N extends Number> WeightedObject<K> from(final Entry<K, N> entry) {
        return new WeightedObject<>(entry.getKey(), entry.getValue().doubleValue());
    }

    public WeightedObject(final KEY objectBeingWeighted, final double totalWeight) {
        this.objectBeingWeighted = objectBeingWeighted;
        this.totalWeight = totalWeight;
    }


    public KEY getObjectBeingWeighted() {
        return objectBeingWeighted;
    }

    public double getTotalWeight() {
        return totalWeight;
    }


    @Override
    public int intValue() {
        return (int) totalWeight;
    }

    @Override
    public long longValue() {
        return (long) totalWeight;
    }

    @Override
    public float floatValue() {
        return (float) totalWeight;
    }

    @Override
    public double doubleValue() {
        return  totalWeight;
    }
}
