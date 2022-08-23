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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Iterables.get;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toCollection;

/**
 * A local biology object based on abundance.
 * It is a container for the number of fish but has no biological processes coded in it.
 * It is quite unsafe as it exposes its arrays in a couple of methods but that is necessary to prevent long delays in copy-pasting
 * abundance data whenever a process takes place
 * Created by carrknight on 3/4/16.
 */
public class AbundanceLocalBiology implements LocalBiology
{


    /**
     * the hashmap contains for each species a table [age][male-female] corresponding to the number of fish of that
     * age and that sex
     */
    private final HashMap<Species, double[][]> abundance = new HashMap<>();

    /**
     * Private empty constructor used for fast creation by
     * {@link #aggregate(GlobalBiology, Collection)}.
     */
    private AbundanceLocalBiology() {
    }

    /**
     * biomass gets computed somewhat lazily (but this number gets reset under any interaction with the object, no matter how trivial)
     */
    private double lastComputedBiomass[];



    /**
     * creates an abundance based local biology that starts off as entirely empty
     * @param biology a GlobalBiology object containing a list of species
     */
    public AbundanceLocalBiology(GlobalBiology biology) {
        this(biology.getSpecies());
    }

    /**
     * creates an abundance based local biology that starts off as entirely empty
     * @param biology a collection of species
     */
    public AbundanceLocalBiology(Collection<Species> allSpecies) {
        //for each species create cohorts
        for (Species species : allSpecies) {
            abundance.put(species, makeAbundanceArray(species));
        }
        //done!
        lastComputedBiomass = new double[allSpecies.size()];
        Arrays.fill(lastComputedBiomass, Double.NaN);
    }

    public static double[][] makeAbundanceArray(final Species species) {
        final double[][] abundanceArray = new double[species.getNumberOfSubdivisions()][];
        for (int i = 0; i < abundanceArray.length; i++) {
            abundanceArray[i] = new double[species.getNumberOfBins()];
        }
        return abundanceArray;
    }

    /**
     * Creates a new abundance object using the provided abundance map.
     *
     * @param abundance A map from species to abundance matrices. The matrices are copied.
     */
    public AbundanceLocalBiology(final Map<Species, double[][]> abundance) {
        abundance.forEach((species, matrix) ->
            this.abundance.put(
                species,
                stream(matrix)
                    .map(a -> Arrays.copyOf(a, a.length))
                    .toArray(double[][]::new)
            )
        );
        lastComputedBiomass = new double[abundance.size()];
        Arrays.fill(lastComputedBiomass, Double.NaN);
    }

    /**
     * Constructs a new AbundanceLocalBiology by making a copy of another.
     *
     * @param other the other AbundanceLocalBiology object to make a copy of.
     */
    @SuppressWarnings("CopyConstructorMissesField") // the call to `this` takes care of that
    public AbundanceLocalBiology(final AbundanceLocalBiology other) {
        this(other.abundance);
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public double getBiomass(Species species) {

        if(Double.isNaN(lastComputedBiomass[species.getIndex()] )) {
            lastComputedBiomass[species.getIndex()] = FishStateUtilities.weigh(
                    abundance.get(species),
                    species.getMeristics()
            );
            assert !Double.isNaN(lastComputedBiomass[species.getIndex()] );
        }
        return lastComputedBiomass[species.getIndex()];

    }

    /**
     * Returns a copied array of the last computed biomass. This method shares name and
     * specification with {@link VariableBiomassBasedBiology#getCurrentBiomass()}
     * even if the current class doesn't implement that interface.
     */
    public double[] getCurrentBiomass() {

        // This is a bit awkward, as we don't have access to the global biology
        // to map indices to species, but should work just fine as long as
        // the abundance map contains all the species (a safe assumption, I think).
        return abundance.keySet()
            .stream()
            .sorted(comparingInt(Species::getIndex))
            .mapToDouble(this::getBiomass)
            .toArray();
    }

    public double getTotalBiomass() {
        return stream(getCurrentBiomass()).sum();
    }

    /**
     * ignored
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Arrays.fill(lastComputedBiomass,Double.NaN);

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }


    private static boolean warned = false;

    /**
     * Will only work if the catch object has biology information
     * @param caught fish taken from the sea
     * @param notDiscarded fish put in hold
     * @param biology biology object
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology)
    {
        Preconditions.checkArgument(caught.hasAbundanceInformation(), "This biology requires a gear that catches per bins rather than biomass directly!");

        for(int index = 0; index < caught.numberOfSpecies(); index++) {
            Species species = biology.getSpecie(index);
            if(species.isImaginary()) //ignore imaginary catches
                continue;

            StructuredAbundance catches = caught.getAbundance(species);
            Preconditions.checkArgument(catches.getSubdivisions()==species.getNumberOfSubdivisions(), "wrong number of cohorts/subdivisions");


            final double[][] abundanceHere = this.abundance.get(species);


            double[][] catchesMatrix = catches.asMatrix();
            Preconditions.checkArgument(catchesMatrix.length == abundanceHere.length);
            Preconditions.checkArgument(catchesMatrix[0].length == abundanceHere[0].length);
            for(int subdivision =0;subdivision<catches.getSubdivisions(); subdivision++ ) {
                for (int bin = 0; bin < catches.getBins(); bin++) {
                    abundanceHere[subdivision][bin] -= catchesMatrix[subdivision][bin];
                    Preconditions.checkArgument(abundanceHere[subdivision][bin] >= -FishStateUtilities.EPSILON,
                                                "There is now a negative amount of male fish left" );
                    //overfished, but could be a numerical issue
                    if(abundanceHere[subdivision][bin]<0)
                    {
                        assert  abundanceHere[subdivision][bin] >= -FishStateUtilities.EPSILON;
                        abundanceHere[subdivision][bin]=0;
                    }

                }
            }
            lastComputedBiomass[species.getIndex()]=Double.NaN;
        }


    }


    @Override
    public StructuredAbundance getAbundance(Species species) {
        Arrays.fill(lastComputedBiomass,Double.NaN); //force a recount after calling this

        return new StructuredAbundance(abundance.get(species)
        );

    }

    public Map<Species, StructuredAbundance> getStructuredAbundance() {
        Arrays.fill(lastComputedBiomass, Double.NaN); // force a recount after calling this
        return abundance.entrySet().stream().collect(toImmutableMap(
            Entry::getKey,
            entry -> new StructuredAbundance(entry.getValue())
        ));
    }

    /**
     * Returns an unmodifiable view of the abundance map, i.e., a map from each {@link Species} to
     * the corresponding abundance matrix. Note that the map itself is unmodifiable but the exposed
     * arrays are not. Mutating those should be done responsibly.
     */
    public Map<Species, double[][]> getAbundance() {
        return Collections.unmodifiableMap(abundance);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("lastComputedBiomass", lastComputedBiomass)
                .toString();
    }

    public static AbundanceLocalBiology aggregate(
        final GlobalBiology globalBiology,
        final Collection<AbundanceLocalBiology> biologies
    ) {
        if (biologies.isEmpty()) return new AbundanceLocalBiology(globalBiology);

        final AbundanceLocalBiology newBiology = new AbundanceLocalBiology();
        newBiology.lastComputedBiomass = sumLastComputedBiomasses(biologies);

        // Grab the abundance map from the first biology as a source for number of bins/subdivisions
        // We don't want to use the GlobalBiology for that as the meristics might not be initialized
        final Map<Species, double[][]> firstAbundance = get(biologies, 0).abundance;
        globalBiology.getSpecies().forEach(species -> {
            final double[][] firstMatrix = firstAbundance.get(species);
            // Make a deep copy of the matrix
            final double[][] newMatrix = stream(firstMatrix)
                .map(a -> copyOf(a, a.length))
                .toArray(double[][]::new);
            biologies.stream().skip(1)
                .map(biology -> biology.abundance.get(species))
                .forEach(otherMatrix -> {
                    for (int sub = 0; sub < newMatrix.length; sub++) {
                        for (int bin = 0; bin < newMatrix[sub].length; bin++) {
                            newMatrix[sub][bin] += otherMatrix[sub][bin];
                        }
                    }
                });
            newBiology.abundance.put(species, newMatrix);
        });
        return newBiology;
    };

    /**
     * Since recomputing biomass is so expensive, we try to take advantage of the
     * precomputed biomass of the biologies we are aggregating (provided we have them)
     */
    private static double [] sumLastComputedBiomasses(
        final Collection<AbundanceLocalBiology> biologies
    ) {
        assert !biologies.isEmpty();
        final Queue<double[]> biomassArrays = biologies.stream()
            .map(b -> b.lastComputedBiomass)
            .collect(toCollection(() -> new ArrayDeque<>(biologies.size())));
        final double[] newBiomassArray = biomassArrays.remove().clone();
        for (int i = 0; i < newBiomassArray.length; i++) {
            for (final double[] biomassArray : biomassArrays) {
                if (Double.isNaN(newBiomassArray[i])) {
                    break;
                }
                newBiomassArray[i] += biomassArray[i];
            }
        }
        return newBiomassArray;
    }

}
