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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import com.google.common.util.concurrent.AtomicDouble;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * a glorified cumulative density function
 */
public class Belief<V> {

    private final TreeMap<V, Double> cdf;


    private double totalWeight = 0;


    public Belief(LinkedList<Particle<V>> particles) {
        assert !particles.isEmpty();

        HashMap<V, AtomicDouble> map = new HashMap<>();
        int observations = particles.size();

        totalWeight = 0;

        for (Particle<V> particle : particles) {
            map.computeIfAbsent(
                particle.getPosition(),
                aDouble -> new AtomicDouble(0)
            ).addAndGet(particle.getWeight());
            totalWeight += particle.getWeight();
        }

        cdf = new TreeMap<>();
        if (totalWeight > 0) {
            for (Map.Entry<V, AtomicDouble> temp : map.entrySet()) {
                cdf.put(temp.getKey(), temp.getValue().doubleValue() / totalWeight);
            }
        }

        assert FishStateUtilities.round(cdf.values().stream().reduce(0.0, Double::sum)) == 1.0;
    }

    public static double[] getSummaryStatistics(Belief<Double> belief) {

        if (belief.cdf.size() == 1)
            return new double[]{belief.cdf.keySet().iterator().next(), 0d};
        if (belief.totalWeight == 0)
            return new double[]{Double.NaN, Double.NaN};
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

    public LinkedList<V> sample(MersenneTwisterFast random, final int sampleSize) {
        assert FishStateUtilities.round(cdf.values().stream().reduce(0.0, Double::sum)) == 1.0;
        ArrayList<Double> randoms = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++)
            randoms.add(random.nextDouble());
        Collections.sort(randoms);
        int samplePos = 0;

        Iterator<Map.Entry<V, Double>> mapIterator = cdf.entrySet().iterator();
        Map.Entry<V, Double> probability = mapIterator.next();
        double total = probability.getValue();
        LinkedList<V> toReturn = new LinkedList<>();

        while (toReturn.size() < sampleSize) {
            if (randoms.get(samplePos) < total) {
                samplePos++;
                toReturn.add(probability.getKey());
            } else {
                probability = mapIterator.next();
                total += probability.getValue();
            }
        }

        return toReturn;

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
