package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import com.google.common.util.concurrent.AtomicDouble;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * a glorified cumulative density function
 */
public class Belief<V>
{

    private final TreeMap<V,Double> cdf;



    private double totalWeight = 0;




    public Belief(LinkedList<Particle<V>> particles) {
        assert !particles.isEmpty();

        HashMap<V,AtomicDouble> map = new HashMap<>();
        int observations = particles.size();

        totalWeight = 0;

        for(Particle<V> particle : particles)
        {
            map.computeIfAbsent(particle.getPosition(),
                                aDouble -> new AtomicDouble(0)).addAndGet(particle.getWeight());
            totalWeight += particle.getWeight();
        }

        cdf = new TreeMap<>();
        if(totalWeight>0) {
            for (Map.Entry<V, AtomicDouble> temp : map.entrySet()) {
                cdf.put(temp.getKey(), temp.getValue().doubleValue() / totalWeight);
            }
        }

        assert FishStateUtilities.round(cdf.values().stream().reduce(0.0, Double::sum))==1.0;
    }


    public LinkedList<V> sample(MersenneTwisterFast random, final int sampleSize)
    {
        assert FishStateUtilities.round(cdf.values().stream().reduce(0.0,Double::sum))==1.0;
        ArrayList<Double> randoms = new ArrayList<>();
        for(int i = 0; i< sampleSize; i++)
            randoms.add(random.nextDouble());
        Collections.sort(randoms);
        int samplePos = 0;

        Iterator<Map.Entry<V, Double>> mapIterator = cdf.entrySet().iterator();
        Map.Entry<V,Double> probability = mapIterator.next();
        double total = probability.getValue();
        LinkedList<V> toReturn = new LinkedList<>();

        while(toReturn.size()< sampleSize)
        {
            if(randoms.get(samplePos) <  total)
            {
                samplePos++;
                toReturn.add(probability.getKey());
            }
            else {
                probability = mapIterator.next();
                total +=  probability.getValue();
            }
        }

        return toReturn;

    }





    public static double[] getSummaryStatistics(Belief<Double> belief)
    {

        if(belief.cdf.size() == 1)
            return new double[]{belief.cdf.keySet().iterator().next(),0d};
        if(belief.totalWeight == 0)
            return new double[]{Double.NaN,Double.NaN};
        else {
            assert FishStateUtilities.round(belief.cdf.values().stream().reduce(0.0, Double::sum)) == 1.0;

            double mean = 0;
            for (Map.Entry<Double, Double> temp : belief.cdf.entrySet()) {
                mean += temp.getKey() * temp.getValue();
            }

            double std = 0;
            for (Map.Entry<Double, Double> temp : belief.cdf.entrySet()) {
                double numerator = mean - temp.getKey();
                std += temp.getValue() * (numerator * numerator);
            }

            return new double[]{mean, Math.sqrt(std)};

        }

    }

    /**
     * Getter for property 'totalWeight'.
     *
     * @return Value for property 'totalWeight'.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Getter for property 'cdf'.
     *
     * @return Value for property 'cdf'.
     */
    public TreeMap<V, Double> getCdf() {
        return cdf;
    }


}
