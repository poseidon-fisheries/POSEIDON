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

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * The map initializer for the two species mpa vs itq example with abundance-driven
 * biology
 * Created by carrknight on 1/20/17.
 */
public class YellowBycatchInitializer implements BiologyInitializer {


    final private boolean separateBycatchStock;


    final private String targetSpeciesName;

    final private String bycatchSpeciesName;


    private final double bycatchRho;
    private final double bycatchNaturalSurvivalRate;
    private final double bycatchRecruitmentSteepness;
    private final int bycatchRecruitmentLag;
    private final double bycatchWeightAtRecruitment;
    private final double bycatchWeightAtRecruitmentMinus1;
    private final double bycatchVirginBiomass;
    private final double bycatchInitialRecruits;


    private final double targetRho;
    private final double targetNaturalSurvivalRate;
    private final double targetRecruitmentSteepness;
    private final int targetRecruitmentLag;
    private final double targetWeightAtRecruitment;
    private final double targetWeightAtRecruitmentMinus1;
    private final double targetVirginBiomass;
    private final double initialVirginRecruits;


    /**
     * any cell with x >= habitatSeparator will include the bycatch species
     */
    final private int habitatSeparator;


    private final int northSouthSeparator;


    /**
     * returns a relative weight (will be normalized by the initializer) describing how much of the
     * initial biomass and K is allocated to each seatile!
     */
    private final Function<SeaTile, Double> allocator;
    private final double targetDiffusionRate;
    /**
     * bycatch tiles
     */
    private final Map<SeaTile, VariableBiomassBasedBiology> bycatchBios = new HashMap<>();
    /**
     * northern tiles
     */
    private final List<VariableBiomassBasedBiology> northBiologies = new LinkedList<>();
    /**
     * southern tiles
     */
    private final List<VariableBiomassBasedBiology> southBiologies = new LinkedList<>();
    /**
     * the original biomass assigned for each local biology. Makes resetting quick
     */
    private final HashMap<VariableBiomassBasedBiology, double[]> originalBiomass = new HashMap<>();
    /**
     * (optional) the biomass for target species in the previous years
     */
    private List<Double> historicalTargetBiomass = null;
    /**
     * (optional) the biomass for bycatch species in the previous years
     */
    private List<Double> historicalBycatchBiomass = null;
    /**
     * (optional) the biomass for target species in the previous years
     */
    private List<Double> historicalTargetSurvivalRate = null;
    /**
     * (optional) the biomass for bycatch species in the previous years
     */
    private List<Double> historicalBycatchSurvivalRate = null;

    public YellowBycatchInitializer(
        final boolean separateBycatchStock,
        final String targetSpeciesName,
        final String bycatchSpeciesName,
        final double bycatchRho,
        final double bycatchNaturalSurvivalRate,
        final double bycatchRecruitmentSteepness,
        final int bycatchRecruitmentLag,
        final double bycatchWeightAtRecruitment,
        final double bycatchWeightAtRecruitmentMinus1,
        final double bycatchVirginBiomass,
        final double bycatchInitialRecruits,
        final double targetRho,
        final double targetNaturalSurvivalRate,
        final double targetRecruitmentSteepness,
        final int targetRecruitmentLag,
        final double targetWeightAtRecruitment,
        final double targetWeightAtRecruitmentMinus1,
        final double targetVirginBiomass,
        final double initialVirginRecruits,
        final int habitatSeparator,
        final int northSouthSeparator,
        final double targetDiffusionRate
    ) {
        this(separateBycatchStock, targetSpeciesName, bycatchSpeciesName, bycatchRho,
            bycatchNaturalSurvivalRate, bycatchRecruitmentSteepness, bycatchRecruitmentLag,
            bycatchWeightAtRecruitment, bycatchWeightAtRecruitmentMinus1, bycatchVirginBiomass,
            bycatchInitialRecruits, targetRho, targetNaturalSurvivalRate,
            targetRecruitmentSteepness, targetRecruitmentLag, targetWeightAtRecruitment,
            targetWeightAtRecruitmentMinus1, targetVirginBiomass, initialVirginRecruits,
            habitatSeparator, northSouthSeparator, seaTile -> 1d, targetDiffusionRate
        );
    }


    public YellowBycatchInitializer(
        final boolean separateBycatchStock,
        final String targetSpeciesName,
        final String bycatchSpeciesName,
        final double bycatchRho,
        final double bycatchNaturalSurvivalRate,
        final double bycatchRecruitmentSteepness,
        final int bycatchRecruitmentLag,
        final double bycatchWeightAtRecruitment,
        final double bycatchWeightAtRecruitmentMinus1,
        final double bycatchVirginBiomass,
        final double bycatchInitialRecruits,
        final double targetRho,
        final double targetNaturalSurvivalRate,
        final double targetRecruitmentSteepness,
        final int targetRecruitmentLag,
        final double targetWeightAtRecruitment,
        final double targetWeightAtRecruitmentMinus1,
        final double targetVirginBiomass,
        final double initialVirginRecruits,
        final int habitatSeparator,
        final int northSouthSeparator,
        final Function<SeaTile, Double> allocator,
        final double targetDiffusionRate
    ) {
        this.separateBycatchStock = separateBycatchStock;
        this.targetSpeciesName = targetSpeciesName;
        this.bycatchSpeciesName = bycatchSpeciesName;
        this.bycatchRho = bycatchRho;
        this.bycatchNaturalSurvivalRate = bycatchNaturalSurvivalRate;
        this.bycatchRecruitmentSteepness = bycatchRecruitmentSteepness;
        this.bycatchRecruitmentLag = bycatchRecruitmentLag;
        this.bycatchWeightAtRecruitment = bycatchWeightAtRecruitment;
        this.bycatchWeightAtRecruitmentMinus1 = bycatchWeightAtRecruitmentMinus1;
        this.bycatchVirginBiomass = bycatchVirginBiomass;
        this.bycatchInitialRecruits = bycatchInitialRecruits;
        this.targetRho = targetRho;
        this.targetNaturalSurvivalRate = targetNaturalSurvivalRate;
        this.targetRecruitmentSteepness = targetRecruitmentSteepness;
        this.targetRecruitmentLag = targetRecruitmentLag;
        this.targetWeightAtRecruitment = targetWeightAtRecruitment;
        this.targetWeightAtRecruitmentMinus1 = targetWeightAtRecruitmentMinus1;
        this.targetVirginBiomass = targetVirginBiomass;
        this.initialVirginRecruits = initialVirginRecruits;
        this.habitatSeparator = habitatSeparator;
        this.northSouthSeparator = northSouthSeparator;
        this.allocator = allocator;
        this.targetDiffusionRate = targetDiffusionRate;
    }


    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
        final GlobalBiology biology,
        final SeaTile seaTile,
        final MersenneTwisterFast random,
        final int mapHeightInCells,
        final int mapWidthInCells,
        final NauticalMap map
    ) {
        if (seaTile.isLand())
            return new EmptyLocalBiology();

        //prepare an empty biology
        //we will change carrying capacities and the like at the end after we have a good count of how many biologies
        //there actually are!
        final BiomassLocalBiology bio = new BiomassLocalBiology(0d, 2, random);

        //will it contain the bycatch?
        if (seaTile.getGridX() >= habitatSeparator) {
            bycatchBios.put(seaTile, bio);

        }

        if (seaTile.getGridY() <= northSouthSeparator)
            northBiologies.add(bio);
        else
            southBiologies.add(bio);
        //return empty biology: we will fill it when processing the map
        return bio;
    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
        final GlobalBiology biology, final NauticalMap map,
        final MersenneTwisterFast random, final FishState model
    ) {

        assert Collections.disjoint(northBiologies, southBiologies); //something is either in tile 1 or tile 2!

        final List<VariableBiomassBasedBiology> allBiologies = new LinkedList<>();
        allBiologies.addAll(northBiologies);
        allBiologies.addAll(southBiologies);

        //split carrying capacity and biomass equally among all tiles
        final int targetContainers = northBiologies.size() + southBiologies.size();
        final int bycatchContainers = bycatchBios.size();
        double targetBiomass = historicalTargetBiomass == null ? targetVirginBiomass :
            historicalTargetBiomass.get(historicalTargetBiomass.size() - 1);
        targetBiomass /= targetContainers;

        //carrying capacity is assigned by weight
        final double carryingCapacityTarget = targetVirginBiomass / targetContainers;
        final double bycatchBiomass = historicalBycatchBiomass == null ?
            bycatchVirginBiomass : historicalBycatchBiomass.get(historicalBycatchBiomass.size() - 1);


        //assign weights!
        final Map<VariableBiomassBasedBiology, Double> weights = new HashMap<>();
        double weightSum = 0;
        for (final Map.Entry<SeaTile, VariableBiomassBasedBiology> bycatchBio : bycatchBios.entrySet()) {
            final double weight = allocator.apply(bycatchBio.getKey());
            weights.put(bycatchBio.getValue(), weight);
            weightSum += weight;
        }

        for (final VariableBiomassBasedBiology bio : allBiologies) {
            bio.setCarryingCapacity(biology.getSpecie(0), carryingCapacityTarget);
            bio.setCurrentBiomass(biology.getSpecie(0), targetBiomass);
            if (bycatchBios.containsValue(bio)) {
                assert weights.containsKey(bio);
                final double weight = weights.get(bio) / weightSum;
                bio.setCarryingCapacity(biology.getSpecie(1), bycatchVirginBiomass * weight);
                bio.setCurrentBiomass(biology.getSpecie(1), bycatchBiomass * weight);
            }
            originalBiomass.put(bio, Arrays.copyOf(bio.getCurrentBiomass(), 2));
        }

        //if no historical biomass is provided, assume the world has always been at virgin levels
        if (historicalTargetBiomass == null)
            historicalTargetBiomass = Collections.nCopies(targetRecruitmentLag, targetVirginBiomass);
        if (historicalBycatchBiomass == null)
            historicalBycatchBiomass = Collections.nCopies(bycatchRecruitmentLag, bycatchVirginBiomass);


        //target grower
        //make sure we allocated the right amount of biomass
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCurrentBiomass()[0]).sum() -
            historicalTargetBiomass.get(historicalTargetBiomass.size() - 1)) < FishStateUtilities.EPSILON;
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCarryingCapacity(0)).sum() -
            targetVirginBiomass) <
            FishStateUtilities.EPSILON;
        final DerisoSchnuteCommonGrower targetGrower = new DerisoSchnuteCommonGrower(
            historicalTargetBiomass,
            historicalTargetSurvivalRate,
            targetRho,
            targetNaturalSurvivalRate,
            targetRecruitmentSteepness,
            targetRecruitmentLag,
            0,
            targetWeightAtRecruitment,
            targetWeightAtRecruitmentMinus1,
            initialVirginRecruits
        );
        targetGrower.getBiologies().addAll(northBiologies);
        targetGrower.getBiologies().addAll(southBiologies);
        model.registerStartable(targetGrower);

        @SuppressWarnings("unchecked") final BiomassDiffuserContainer diffuser =
            new BiomassDiffuserContainer(map, model.getRandom(),
                biology,
                entry(
                    biology.getSpecie(0),
                    new SmoothMovementRule(
                        targetDiffusionRate,
                        .1
                    )
                )
            ); //only sablefish moves!
        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);

        assert Math.abs(bycatchBios.values().stream().mapToDouble(value -> value.getCurrentBiomass()[1]).sum() -
            historicalBycatchBiomass.get(historicalBycatchBiomass.size() - 1)) < FishStateUtilities.EPSILON;
        assert Math.abs(bycatchBios.values()
            .stream()
            .mapToDouble(value -> value.getCarryingCapacity(1))
            .sum() - bycatchVirginBiomass) < FishStateUtilities.EPSILON;
        //bycatch growers
        if (!separateBycatchStock) {
            //unified grower, very similar to the one for target fish

            final DerisoSchnuteCommonGrower unifiedBycatchGrower = new DerisoSchnuteCommonGrower(
                historicalBycatchBiomass,
                historicalBycatchSurvivalRate,
                bycatchRho,
                bycatchNaturalSurvivalRate,
                bycatchRecruitmentSteepness,
                bycatchRecruitmentLag,
                1,
                bycatchWeightAtRecruitment,
                bycatchWeightAtRecruitmentMinus1,
                bycatchInitialRecruits
            );
            unifiedBycatchGrower.getBiologies().addAll(bycatchBios.values());
            model.registerStartable(unifiedBycatchGrower);
        } else {

            final List<VariableBiomassBasedBiology> southBycatch = new LinkedList<>(bycatchBios.values());
            southBycatch.removeAll(northBiologies);
            final List<VariableBiomassBasedBiology> northBycatch = new LinkedList<>(bycatchBios.values());
            northBycatch.removeAll(southBiologies);


            final double virginNorth = northBiologies.stream().mapToDouble(value -> value.getCurrentBiomass()[1]).sum();
            final double proportionNorth = virginNorth / bycatchVirginBiomass;


            //historical bycatch, if not provided then it's just the virgin value
            assert historicalBycatchBiomass != null;

            final List<Double> historyNorth = new ArrayList<>(historicalBycatchBiomass);
            historyNorth.replaceAll(biomass -> biomass * proportionNorth);


            final DerisoSchnuteCommonGrower northGrower = new DerisoSchnuteCommonGrower(
                historyNorth,
                historicalBycatchSurvivalRate,
                bycatchRho,
                bycatchNaturalSurvivalRate,
                bycatchRecruitmentSteepness,
                bycatchRecruitmentLag,
                1,
                bycatchWeightAtRecruitment,
                bycatchWeightAtRecruitmentMinus1,
                bycatchInitialRecruits * proportionNorth
            );
            northGrower.getBiologies().addAll(northBiologies);
            model.registerStartable(northGrower);


            final List<Double> historySouth = new ArrayList<>(historicalBycatchBiomass);
            historySouth.replaceAll(biomass -> biomass * (1d - proportionNorth));


            final DerisoSchnuteCommonGrower southGrower = new DerisoSchnuteCommonGrower(
                historySouth,
                historicalBycatchSurvivalRate,
                bycatchRho,
                bycatchNaturalSurvivalRate,
                bycatchRecruitmentSteepness,
                bycatchRecruitmentLag,
                1,
                bycatchWeightAtRecruitment,
                bycatchWeightAtRecruitmentMinus1,
                bycatchInitialRecruits * (1d - proportionNorth)
            );
            northGrower.getBiologies().addAll(southBiologies);
            model.registerStartable(southGrower);


        }


    }


    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
        final MersenneTwisterFast random,
        final FishState modelBeingInitialized
    ) {
        final StockAssessmentCaliforniaMeristics fake = new StockAssessmentCaliforniaMeristics(
            1,
            1,
            0,
            0,
            1,
            1,
            1,
            0,
            1,
            0,
            0,
            1,
            1,
            1,
            0,
            1,
            0,
            0,
            0,
            0,
            1,
            1,
            false
        );
        final Species target = new Species(
            targetSpeciesName,
            fake
        );
        final Species bycatch = new Species(
            bycatchSpeciesName,
            fake
        );

        return new GlobalBiology(target, bycatch);

    }


    public boolean isSeparateBycatchStock() {
        return separateBycatchStock;
    }

    public String getTargetSpeciesName() {
        return targetSpeciesName;
    }

    public String getBycatchSpeciesName() {
        return bycatchSpeciesName;
    }

    public double getBycatchRho() {
        return bycatchRho;
    }

    public double getBycatchNaturalSurvivalRate() {
        return bycatchNaturalSurvivalRate;
    }

    public double getBycatchRecruitmentSteepness() {
        return bycatchRecruitmentSteepness;
    }

    public int getBycatchRecruitmentLag() {
        return bycatchRecruitmentLag;
    }

    public double getBycatchWeightAtRecruitment() {
        return bycatchWeightAtRecruitment;
    }

    public double getBycatchWeightAtRecruitmentMinus1() {
        return bycatchWeightAtRecruitmentMinus1;
    }

    public double getBycatchVirginBiomass() {
        return bycatchVirginBiomass;
    }

    public double getBycatchInitialRecruits() {
        return bycatchInitialRecruits;
    }

    public double getTargetRho() {
        return targetRho;
    }

    public double getTargetNaturalSurvivalRate() {
        return targetNaturalSurvivalRate;
    }

    public double getTargetRecruitmentSteepness() {
        return targetRecruitmentSteepness;
    }

    public int getTargetRecruitmentLag() {
        return targetRecruitmentLag;
    }

    public double getTargetWeightAtRecruitment() {
        return targetWeightAtRecruitment;
    }

    public double getTargetWeightAtRecruitmentMinus1() {
        return targetWeightAtRecruitmentMinus1;
    }

    public double getTargetVirginBiomass() {
        return targetVirginBiomass;
    }

    public double getInitialVirginRecruits() {
        return initialVirginRecruits;
    }

    public int getHabitatSeparator() {
        return habitatSeparator;
    }

    public Collection<VariableBiomassBasedBiology> getBycatchBios() {
        return bycatchBios.values();
    }

    public List<VariableBiomassBasedBiology> getNorthBiologies() {
        return northBiologies;
    }

    public List<VariableBiomassBasedBiology> getSouthBiologies() {
        return southBiologies;
    }


    /**
     * Getter for property 'historicalTargetBiomass'.
     *
     * @return Value for property 'historicalTargetBiomass'.
     */
    public List<Double> getHistoricalTargetBiomass() {
        return historicalTargetBiomass;
    }

    /**
     * Setter for property 'historicalTargetBiomass'.
     *
     * @param historicalTargetBiomass Value to set for property 'historicalTargetBiomass'.
     */
    public void setHistoricalTargetBiomass(final List<Double> historicalTargetBiomass) {
        this.historicalTargetBiomass = historicalTargetBiomass;
    }

    /**
     * Getter for property 'historicalBycatchBiomass'.
     *
     * @return Value for property 'historicalBycatchBiomass'.
     */
    public List<Double> getHistoricalBycatchBiomass() {
        return historicalBycatchBiomass;
    }

    /**
     * Setter for property 'historicalBycatchBiomass'.
     *
     * @param historicalBycatchBiomass Value to set for property 'historicalBycatchBiomass'.
     */
    public void setHistoricalBycatchBiomass(final List<Double> historicalBycatchBiomass) {
        this.historicalBycatchBiomass = historicalBycatchBiomass;
    }


    /**
     * Getter for property 'historicalTargetSurvivalRate'.
     *
     * @return Value for property 'historicalTargetSurvivalRate'.
     */
    public List<Double> getHistoricalTargetSurvivalRate() {
        return historicalTargetSurvivalRate;
    }

    /**
     * Setter for property 'historicalTargetSurvivalRate'.
     *
     * @param historicalTargetSurvivalRate Value to set for property 'historicalTargetSurvivalRate'.
     */
    public void setHistoricalTargetSurvivalRate(final List<Double> historicalTargetSurvivalRate) {
        this.historicalTargetSurvivalRate = historicalTargetSurvivalRate;
    }

    /**
     * Getter for property 'historicalBycatchSurvivalRate'.
     *
     * @return Value for property 'historicalBycatchSurvivalRate'.
     */
    public List<Double> getHistoricalBycatchSurvivalRate() {
        return historicalBycatchSurvivalRate;
    }

    /**
     * Setter for property 'historicalBycatchSurvivalRate'.
     *
     * @param historicalBycatchSurvivalRate Value to set for property 'historicalBycatchSurvivalRate'.
     */
    public void setHistoricalBycatchSurvivalRate(final List<Double> historicalBycatchSurvivalRate) {
        this.historicalBycatchSurvivalRate = historicalBycatchSurvivalRate;
    }

    /**
     * Getter for property 'northSouthSeparator'.
     *
     * @return Value for property 'northSouthSeparator'.
     */
    public int getNorthSouthSeparator() {
        return northSouthSeparator;
    }

    /**
     * you must at all time be ready to reset local biology to its pristine state
     *
     * @param species species you want the biomass resetted
     */
    public void resetLocalBiology(final Species species) {

        //reset!
        for (final Map.Entry<VariableBiomassBasedBiology, double[]> local : originalBiomass.entrySet()) {
            local.getKey().setCurrentBiomass(species, local.getValue()[species.getIndex()]);

        }

    }


}
