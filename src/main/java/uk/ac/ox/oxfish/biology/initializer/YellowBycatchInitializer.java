package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * The map initializer for the two species mpa vs itq example with abundance-driven
 * biology
 * Created by carrknight on 1/20/17.
 */
public class YellowBycatchInitializer implements BiologyInitializer {


    final private boolean separateBycatchStock;


    final private String targetSpeciesName;

    final private String bycatchSpeciesName;


    private final double bycatchRho ;
    private final double bycatchNaturalSurvivalRate ;
    private final double bycatchRecruitmentSteepness;
    private final int bycatchRecruitmentLag;
    private final double bycatchWeightAtRecruitment;
    private final double bycatchWeightAtRecruitmentMinus1;
    private final double bycatchVirginBiomass;
    private final double bycatchInitialRecruits;



    private final double targetRho ;
    private final double targetNaturalSurvivalRate ;
    private final double targetRecruitmentSteepness;
    private final int targetRecruitmentLag;
    private final double targetWeightAtRecruitment;
    private final double targetWeightAtRecruitmentMinus1;
    private final double targetVirginBiomass;
    private final double initialVirginRecruits;



    /**
     * any cell with x >= verticalSeparator will include the bycatch species
     */
    final private int verticalSeparator;

    /**
     * bycatch tiles
     */
    private List<BiomassLocalBiology> bycatchBios = new LinkedList<>();

    /**
     * northern tiles
     */
    private List<BiomassLocalBiology> northBiologies = new LinkedList<>();
    /**
     * southern tiles
     */
    private List<BiomassLocalBiology> southBiologies = new LinkedList<>();


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
            boolean separateBycatchStock, String targetSpeciesName, String bycatchSpeciesName, double bycatchRho,
            double bycatchNaturalSurvivalRate, double bycatchRecruitmentSteepness, int bycatchRecruitmentLag,
            double bycatchWeightAtRecruitment, double bycatchWeightAtRecruitmentMinus1, double bycatchVirginBiomass,
            double bycatchInitialRecruits, double targetRho, double targetNaturalSurvivalRate,
            double targetRecruitmentSteepness, int targetRecruitmentLag, double targetWeightAtRecruitment,
            double targetWeightAtRecruitmentMinus1, double targetVirginBiomass, double initialVirginRecruits,
            int verticalSeparator) {
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
        this.verticalSeparator = verticalSeparator;
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {
        if(seaTile.getAltitude()>=0)
            return new EmptyLocalBiology();

        //prepare an empty biology
        //we will change carrying capacities and the like at the end after we have a good count of how many biologies
        //there actually are!
        BiomassLocalBiology bio = new BiomassLocalBiology(0d,2,random);

        //will it contain the bycatch?
        if(seaTile.getGridX()>=verticalSeparator) {
            bycatchBios.add(bio);

        }

        if(seaTile.getGridY()<=mapHeightInCells/2)
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
            GlobalBiology biology, NauticalMap map,
            MersenneTwisterFast random, FishState model) {

        assert  Collections.disjoint(northBiologies,southBiologies); //something is either in tile 1 or tile 2!

        List<BiomassLocalBiology> allBiologies = new LinkedList<>();
        allBiologies.addAll(northBiologies);
        allBiologies.addAll(southBiologies);

        //split carrying capacity and biomass equally among all tiles
        int targetContainers = northBiologies.size() + southBiologies.size();
        int bycatchContainers = bycatchBios.size();
        double targetBiomass = historicalTargetBiomass == null ? targetVirginBiomass :
                historicalTargetBiomass.get(historicalTargetBiomass.size()-1);
        targetBiomass /= targetContainers;
        double carryingCapacityTarget = targetVirginBiomass / targetContainers;
        double bycatchBiomass = historicalBycatchBiomass  == null ?
                bycatchVirginBiomass : historicalBycatchBiomass.get(historicalBycatchBiomass.size()-1);
        double carryingCapacityBycatch = bycatchVirginBiomass / bycatchContainers;
        bycatchBiomass /= bycatchContainers;


        for(BiomassLocalBiology bio : allBiologies)
        {
            bio.setCarryingCapacity(biology.getSpecie(0),carryingCapacityTarget);
            bio.setCurrentBiomass(biology.getSpecie(0),targetBiomass);
            if(bycatchBios.contains(bio))
            {
                bio.setCarryingCapacity(biology.getSpecie(1),carryingCapacityBycatch);
                bio.setCurrentBiomass(biology.getSpecie(1),bycatchBiomass);
            }
        }

        //if no historical biomass is provided, assume the world has always been at virgin levels
        if(historicalTargetBiomass==null)
            historicalTargetBiomass= Collections.nCopies(targetRecruitmentLag, targetVirginBiomass);
        if(historicalBycatchBiomass == null)
            historicalBycatchBiomass = Collections.nCopies(bycatchRecruitmentLag,bycatchVirginBiomass);


        //target grower
        //make sure we allocated the right amount of biomass
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCurrentBiomass()[0]).sum() -
                                historicalTargetBiomass.get(historicalTargetBiomass.size()-1)) < FishStateUtilities.EPSILON;
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCarryingCapacity(0)).sum() - targetVirginBiomass) < FishStateUtilities.EPSILON;
        DerisoSchnuteCommonGrower targetGrower = new DerisoSchnuteCommonGrower(
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

        assert Math.abs(bycatchBios.stream().mapToDouble(value -> value.getCurrentBiomass()[1]).sum() -
                                historicalBycatchBiomass.get(historicalBycatchBiomass.size()-1)) < FishStateUtilities.EPSILON;
        assert Math.abs(bycatchBios.stream().mapToDouble(value -> value.getCarryingCapacity(1)).sum() - bycatchVirginBiomass) < FishStateUtilities.EPSILON;
        //bycatch growers
        if(!separateBycatchStock)
        {
            //unified grower, very similar to the one for target fish

            DerisoSchnuteCommonGrower unifiedBycatchGrower = new DerisoSchnuteCommonGrower(
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
            unifiedBycatchGrower.getBiologies().addAll(bycatchBios);
            model.registerStartable(unifiedBycatchGrower);
        }
        else
        {

            List<BiomassLocalBiology> southBycatch = new LinkedList<>(bycatchBios);
            southBycatch.removeAll(northBiologies);
            List<BiomassLocalBiology> northBycatch = new LinkedList<>(bycatchBios);
            northBycatch.removeAll(southBiologies);


            double virginNorth = northBiologies.stream().mapToDouble(value -> value.getCurrentBiomass()[1]).sum();
            double proportionNorth = virginNorth / bycatchVirginBiomass;


            //historical bycatch, if not provided then it's just the virgin value
            assert historicalBycatchBiomass!=null;

            List<Double> historyNorth = new ArrayList<>(historicalBycatchBiomass);
            historyNorth.replaceAll(biomass -> biomass * proportionNorth);


            DerisoSchnuteCommonGrower northGrower = new DerisoSchnuteCommonGrower(
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


            List<Double> historySouth = new ArrayList<>(historicalBycatchBiomass);
            historySouth.replaceAll(biomass -> biomass * (1d-proportionNorth));



            DerisoSchnuteCommonGrower southGrower = new DerisoSchnuteCommonGrower(
                    historySouth,
                    historicalBycatchSurvivalRate,
                    bycatchRho,
                    bycatchNaturalSurvivalRate,
                    bycatchRecruitmentSteepness,
                    bycatchRecruitmentLag,
                    1,
                    bycatchWeightAtRecruitment,
                    bycatchWeightAtRecruitmentMinus1,
                    bycatchInitialRecruits * (1d-proportionNorth)
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
            MersenneTwisterFast random,
            FishState modelBeingInitialized) {
        Meristics fake = new Meristics(1,
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
                                       false);
        Species target = new Species(targetSpeciesName,
                                     fake);
        Species bycatch = new Species(bycatchSpeciesName,
                                      fake);

        return new GlobalBiology(target,bycatch);

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

    public int getVerticalSeparator() {
        return verticalSeparator;
    }

    public List<BiomassLocalBiology> getBycatchBios() {
        return bycatchBios;
    }

    public List<BiomassLocalBiology> getNorthBiologies() {
        return northBiologies;
    }

    public List<BiomassLocalBiology> getSouthBiologies() {
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
    public void setHistoricalTargetBiomass(List<Double> historicalTargetBiomass) {
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
    public void setHistoricalBycatchBiomass(List<Double> historicalBycatchBiomass) {
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
    public void setHistoricalTargetSurvivalRate(List<Double> historicalTargetSurvivalRate) {
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
    public void setHistoricalBycatchSurvivalRate(List<Double> historicalBycatchSurvivalRate) {
        this.historicalBycatchSurvivalRate = historicalBycatchSurvivalRate;
    }
}