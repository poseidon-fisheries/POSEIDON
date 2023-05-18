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

package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Right now this is just a map specie--->pounds caught. It might in the future deal with age and other factors which is
 * why I create the object catch rather than just using a map
 * Created by carrknight on 4/20/15.
 */
public class Catch {


    private final double[] biomassCaught;

    /**
     * optionally this object can contain the age structure of the catch
     * first index is species <br>
     */
    private final StructuredAbundance[] abundance;


    private final double totalWeight;


    /**
     * single species catch
     *
     * @param species      the species caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(final Species species, final double poundsCaught, final GlobalBiology biology) {
        this(species, poundsCaught, biology.getSize());


    }

    /**
     * single species catch
     *
     * @param species      the species caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(final Species species, final double poundsCaught, final int numberOfSpeciesInTheModel) {
        Preconditions.checkState(poundsCaught >= 0);
        biomassCaught = new double[numberOfSpeciesInTheModel];
        biomassCaught[species.getIndex()] = poundsCaught;
        abundance = null;

        totalWeight = poundsCaught;


    }

    /**
     * create a catch object given the abundance of each species binned per age/length
     *
     * @param ageStructure binned abundance per species
     * @param biology      the biology object containing info about each species
     */
    public Catch(final double[][] ageStructure, final GlobalBiology biology) {
        Preconditions.checkArgument(biology.getSize() == ageStructure.length);
        this.abundance = new StructuredAbundance[ageStructure.length];
        for (int i = 0; i < ageStructure.length; i++)
            abundance[i] = new StructuredAbundance(ageStructure[i]);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    private double[] abundanceToBiomass(final GlobalBiology biology) {
        final double[] biomasses = new double[biology.getSize()];
        for (final Species species : biology.getSpecies())
            biomasses[species.getIndex()] =
                FishStateUtilities.weigh(
                    abundance[species.getIndex()],
                    species.getMeristics()
                );
        return biomasses;
    }

    private double computeTotalWeight() {
        double weight = 0;
        for (final double biomass : biomassCaught) {
            Preconditions.checkArgument(biomass >= 0, "can't fish negative weight!, caught: " + biomass);
            weight += biomass;
        }
        return weight;
    }

    /**
     * @param maleAbundance   male abundance per species per bin
     * @param femaleAbundance female abundance per species per bin
     * @param biology         biology
     */
    public Catch(final double[][] maleAbundance, final double[][] femaleAbundance, final GlobalBiology biology) {
        Preconditions.checkArgument(biology.getSize() == maleAbundance.length);
        this.abundance = new StructuredAbundance[maleAbundance.length];
        for (int i = 0; i < maleAbundance.length; i++)
            abundance[i] = new StructuredAbundance(maleAbundance[i], femaleAbundance[i]);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);

        totalWeight = computeTotalWeight();

    }

    /**
     * single species abundance catch
     *
     * @param maleAbundance
     * @param femaleAbundance
     * @param correctSpecies
     * @param biology
     */
    public Catch(final double[] maleAbundance, final double[] femaleAbundance, final Species correctSpecies, final GlobalBiology biology) {
        this.abundance = new StructuredAbundance[biology.getSize()];
        for (final Species index : biology.getSpecies()) {
            if (correctSpecies == index)
                abundance[index.getIndex()] = new StructuredAbundance(maleAbundance, femaleAbundance);
            else
                abundance[index.getIndex()] = new StructuredAbundance(
                    new double[index.getNumberOfBins()],
                    new double[index.getNumberOfBins()]
                );
        }
        //weigh them (assuming they are all men!)
        biomassCaught = new double[biology.getSize()];
        for (final Species species : biology.getSpecies())
            biomassCaught[species.getIndex()] =
                FishStateUtilities.weigh(
                    abundance[species.getIndex()],
                    species.getMeristics()
                );
        totalWeight = computeTotalWeight();

    }

    /**
     * Constructs a catch object capturing the whole biomass of the target biology.
     *
     * @param biology The target biology.
     */
    public Catch(final VariableBiomassBasedBiology biology) {
        this(biology.getCurrentBiomass());
    }

    public Catch(final double[] catches) {

        this.biomassCaught = catches;
        abundance = null;
        totalWeight = computeTotalWeight();

    }

    public Catch(
        final GlobalBiology globalBiology, final AbundanceLocalBiology caughtBiology
    ) {
        this(
            globalBiology
                .getSpecies()
                .stream()
                .map(species -> new StructuredAbundance(caughtBiology.getAbundance(species)))
                .toArray(StructuredAbundance[]::new),
            globalBiology
        );
    }

    /**
     * create a catch object given the abundance of each species binned per age/length
     *
     * @param abundance binned abundance per species
     * @param biology   the biology object containing info about each species
     */
    public Catch(final StructuredAbundance[] abundance, final GlobalBiology biology) {
        this.abundance = abundance;
        Preconditions.checkArgument(biology.getSize() == abundance.length);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    /**
     * returns a new Catch object which represents the sum of two separate catch objects.
     * It assumes that both inputs are congruent (they either both have abundance information or they both don't).
     * THIS IS NOT SAFE AND WILL RUIN FIRST CATCH numbers; save ahead!
     *
     * @param first  the first catch to sum (WILL BE MODIFIED AS SIDE EFFECT)
     * @param second
     * @return
     */
    public static Catch sumCatches(
        final Catch first,
        final Catch second
    ) {
        //one might be empty, if so skip all this


        if (first.hasAbundanceInformation()) {
            if (!second.hasAbundanceInformation() && second.getTotalWeight() == 0)
                //second is empty, it's okay
                return first;

            Preconditions.checkState(second.hasAbundanceInformation(), "cannot sum up incongruent catches!");
            for (int species = 0; species < first.abundance.length; species++) {
                final double[][] matrixAbundance = first.abundance[species].asMatrix();
                final double[][] toAdd = second.abundance[species].asMatrix();
                for (int i = 0; i < matrixAbundance.length; i++)
                    for (int j = 0; j < toAdd.length; j++)
                        matrixAbundance[i][j] += toAdd[i][j];

            }
            return first;

        } else {
            if (second.hasAbundanceInformation() && first.getTotalWeight() == 0)
                //first is empty, it's okay
                return second;

            Preconditions.checkState(!second.hasAbundanceInformation(), "cannot sum up incongruent catches!");

            final double[] biomass = new double[first.biomassCaught.length];
            for (int i = 0; i < biomass.length; i++)
                biomass[i] = first.biomassCaught[i] + second.biomassCaught[i];
            return new Catch(biomass);


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

    public double getWeightCaught(final Species species) {
        return biomassCaught[species.getIndex()];
    }

    public double getWeightCaught(final int index) {
        return biomassCaught[index];
    }

    public int numberOfSpecies() {
        return biomassCaught.length;
    }

    public double getWeightCaught(final Species species, final int bin) {
        Preconditions.checkArgument(hasAbundanceInformation());
        return FishStateUtilities.weigh(abundance[species.getIndex()], species.getMeristics(), bin);
    }

    public boolean hasAbundanceInformation() {
        return abundance != null;
    }

    public double getWeightCaught(final Species species, final int subdivision, final int bin) {
        Preconditions.checkArgument(hasAbundanceInformation());
        return FishStateUtilities.weigh(abundance[species.getIndex()], species.getMeristics(), subdivision, bin);
    }

    public StructuredAbundance getAbundance(final Species species) {
        return getAbundance(species.getIndex());
    }

    public StructuredAbundance getAbundance(final int index) {
        if (abundance == null)
            return null;
        return abundance[index];
    }

    public double totalCatchWeight() {
        return totalWeight;
    }

    @Override
    public String toString() {
        return Arrays.toString(biomassCaught);

    }

    /**
     * returns a copy of the biomass copies
     */
    public double[] getBiomassArray() {
        return Arrays.copyOf(biomassCaught, biomassCaught.length);
    }
}
