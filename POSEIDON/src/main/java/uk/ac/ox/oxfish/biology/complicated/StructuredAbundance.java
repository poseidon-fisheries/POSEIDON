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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleBiFunction;

import static java.util.stream.IntStream.range;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.*;

/**
 * A container for an abundance metric where we expect
 * the # of fish to be classified by length/age (anyway bins) and
 * possibly also by subcategories (like male/female)
 * <p>
 * <p>
 * None of these arrays are copies, these are all live pointers
 * Created by carrknight on 5/2/17.
 */
public class StructuredAbundance {


    /**
     * abundance, per subdivision per bin
     */
    private double[][] abundance;


    /**
     * create simple abundance as vector where each element represents a
     * length/age bin
     *
     * @param subdivision0
     */
    public StructuredAbundance(double[] subdivision0) {
        //Preconditions.checkArgument(ageStructure.length > 0); not true anymore since it could be an empty
        // biology forced to return an empty structure
        abundance = new double[1][];
        abundance[0] = subdivision0;
    }

    public StructuredAbundance(double[][] abundance) {
        this.abundance = abundance;
    }

    public StructuredAbundance(
        double[] maleAbundance,
        double[] femaleAbundance
    ) {

        Preconditions.checkArgument(maleAbundance.length == femaleAbundance.length);
        Preconditions.checkArgument(maleAbundance.length > 0);
        abundance = new double[2][];
        abundance[MALE] = maleAbundance;
        abundance[FEMALE] = femaleAbundance;
    }


    /**
     * empty abundance
     *
     * @param subdivisions
     * @param bins
     */
    public StructuredAbundance(int subdivisions, int bins) {
        Preconditions.checkArgument(subdivisions > 0);
        abundance = new double[subdivisions][];
        for (int i = 0; i < subdivisions; i++)
            abundance[i] = new double[bins];
    }

    public StructuredAbundance(StructuredAbundance other) {
        this.abundance = new double[other.getSubdivisions()][other.getBins()];
        for (int i = 0; i < abundance.length; i++)
            for (int j = 0; j < abundance[i].length; j++) {
                abundance[i][j] = other.abundance[i][j];
            }
    }

    public int getSubdivisions() {
        return abundance.length;
    }

    public int getBins() {
        return abundance[0].length;
    }

    public static StructuredAbundance empty(final Species species) {
        final int subs = species.getNumberOfSubdivisions();
        final int bins = species.getNumberOfBins();
        return new StructuredAbundance(subs, bins);
    }

    public static StructuredAbundance sum(
        Iterable<StructuredAbundance> abundances,
        int bins, int subdivisions
    ) {

        StructuredAbundance total = new StructuredAbundance(subdivisions, bins);
        for (StructuredAbundance abundance : abundances) {
            for (int subdivision = 0; subdivision < subdivisions; subdivision++) {
                for (int bin = 0; bin < bins; bin++) {
                    total.asMatrix()[subdivision][bin] += abundance.asMatrix()[subdivision][bin];
                }
            }
        }


        return total;
    }

    /**
     * get the age structured matrix
     *
     * @return
     */
    public double[][] asMatrix() {
        return abundance;
    }

    public double getAbundanceInBin(int bin) {
        double fish = 0;
        for (int group = 0; group < getSubdivisions(); group++)
            fish += abundance[group][bin];
        return fish;
    }

    /**
     * compute weight of structured abundance assuming it's referring to this species
     *
     * @param species species the abundance is referring to
     * @return a weight
     */
    public double computeWeight(Species species) {
        return FishStateUtilities.weigh(this, species.getMeristics());


    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("abundance", abundance)
            .toString();
    }

    public Entry<StructuredAbundance, Double> mapAndWeigh(
        final Species species,
        final ToDoubleBiFunction<Integer, Integer> mapper
    ) {
        final AtomicDouble totalWeight = new AtomicDouble();
        final StructuredAbundance structuredAbundance =
            mapIndices((subDivision, bin) -> {
                final double binAbundance = mapper.applyAsDouble(subDivision, bin);
                totalWeight.addAndGet(binAbundance * species.getWeight(subDivision, bin));
                return binAbundance;
            });
        return entry(structuredAbundance, totalWeight.get());
    }

    /**
     * Creates a new structured abundance by applying a mapper function that takes the subdivision
     * and the bin as parameters.
     *
     * @param mapper a mapper function that takes the subdivision and the bin as parameters.
     * @return a new StructuredAbundance.
     */
    public StructuredAbundance mapIndices(final ToDoubleBiFunction<Integer, Integer> mapper) {


        double[][] abundance = new double[getSubdivisions()][getBins()];
        for (int subdivision = 0; subdivision < abundance.length; subdivision++) {
            for (int bin = 0; bin < abundance[subdivision].length; bin++) {
                abundance[subdivision][bin] = mapper.applyAsDouble(subdivision, bin);
            }
        }
        return new StructuredAbundance(abundance);

    }

    public void forEachIndex(final BiConsumer<Integer, Integer> consumer) {
        range(0, getSubdivisions()).forEach(subDivision ->
            range(0, getBins()).forEach(bin ->
                consumer.accept(subDivision, bin)
            )
        );
    }

    public StructuredAbundance mapValues(final DoubleUnaryOperator mapper) {
        return mapIndices((sub, bin) -> mapper.applyAsDouble(getAbundance(sub, bin)));
    }

    /**
     * get one element from the Abundance matrix
     *
     * @param subdivision the group (usually MALE and FEMALE)
     * @param bin         the age/length bin
     * @return the abundance number
     */
    public double getAbundance(int subdivision, int bin) {
        return abundance[subdivision][bin];
    }
}
