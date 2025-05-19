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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import ec.util.MersenneTwisterFast;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * The particle filter object. Contains a list of particles and allows for new evidence to come in and for time to pass
 * Created by carrknight on 7/29/16.
 */
public class ParticleFilter<T> {


    private final LinkedList<Particle<T>> particles = new LinkedList<>();

    /**
     * the function to create new random particle position when initializing or when resetting
     */
    private final Function<MersenneTwisterFast, T> particleGenerator;

    /**
     * the function applies to each particle position to simulate elapsing time (which usually implies less knowledge)
     */
    private final Function<T, T> drifter;


    public ParticleFilter(
        Function<MersenneTwisterFast, T> particleGenerator,
        Function<T, T> drifter,
        int size, MersenneTwisterFast random
    ) {

        this.drifter = drifter;
        this.particleGenerator = particleGenerator;
        initialize(size, random);
    }

    /**
     * initializes the filter uniformly
     *
     * @param size
     * @param random
     */
    private void initialize(int size, MersenneTwisterFast random) {

        particles.clear();
        for (int i = 0; i < size; i++)
            particles.add(new Particle<>(particleGenerator.apply(random)));

    }

    public static ParticleFilter<Double> defaultParticleFilter(
        double min, double max, double drift,
        int size, MersenneTwisterFast random
    ) {
        return new ParticleFilter<>(
            mersenneTwisterFast -> mersenneTwisterFast.nextDouble() * (max - min) + min,
            previous -> Math.max(
                Math.min(previous + random.nextGaussian() * drift, max), min),
            size, random
        );

    }

    /**
     * this is the "update" phase of the particle filter given the evidence. It will weight and resample the particles
     *
     * @param evidenceProbability the conditional probability p(e|x) as a function that is given x and returns p(e)
     */
    public void updateGivenEvidence(
        Function<T, Double> evidenceProbability,
        MersenneTwisterFast randomizer
    ) {
        //reweight
        for (Particle<T> particle : particles)
            particle.setWeight(evidenceProbability.apply(particle.getPosition()));
        //resample
        Belief<T> belief = getBelief();
        //if the weight of all particles is 0 then reset
        if (belief.getTotalWeight() <= 0)
            initialize(particles.size(), randomizer);
        else {
            LinkedList<T> sample = belief.sample(randomizer, particles.size());
            for (int i = 0; i < particles.size(); i++) {
                Particle<T> particle = particles.get(i);
                particle.setPosition(sample.get(i));
                particle.setWeight(1);
            }
        }

    }

    public Belief<T> getBelief() {

        return new Belief<T>(particles);
    }

    /**
     * adds noise to each particle, that's our time elapse phase
     */
    public void drift(MersenneTwisterFast random) {
        for (Particle<T> particle : particles)
            particle.setPosition(
                drifter.apply(particle.getPosition()));

    }


}
