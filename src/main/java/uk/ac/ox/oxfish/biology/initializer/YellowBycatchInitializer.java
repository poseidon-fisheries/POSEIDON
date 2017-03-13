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
    private final double bycatchVirginRecruits;



    private final double targetRho ;
    private final double targetNaturalSurvivalRate ;
    private final double targetRecruitmentSteepness;
    private final int targetRecruitmentLag;
    private final double targetWeightAtRecruitment;
    private final double targetWeightAtRecruitmentMinus1;
    private final double targetVirginBiomass;
    private final double targetVirginRecruits;



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


    public YellowBycatchInitializer(
            boolean separateBycatchStock, String targetSpeciesName, String bycatchSpeciesName, double bycatchRho,
            double bycatchNaturalSurvivalRate, double bycatchRecruitmentSteepness, int bycatchRecruitmentLag,
            double bycatchWeightAtRecruitment, double bycatchWeightAtRecruitmentMinus1, double bycatchVirginBiomass,
            double bycatchVirginRecruits, double targetRho, double targetNaturalSurvivalRate,
            double targetRecruitmentSteepness, int targetRecruitmentLag, double targetWeightAtRecruitment,
            double targetWeightAtRecruitmentMinus1, double targetVirginBiomass, double targetVirginRecruits,
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
        this.bycatchVirginRecruits = bycatchVirginRecruits;
        this.targetRho = targetRho;
        this.targetNaturalSurvivalRate = targetNaturalSurvivalRate;
        this.targetRecruitmentSteepness = targetRecruitmentSteepness;
        this.targetRecruitmentLag = targetRecruitmentLag;
        this.targetWeightAtRecruitment = targetWeightAtRecruitment;
        this.targetWeightAtRecruitmentMinus1 = targetWeightAtRecruitmentMinus1;
        this.targetVirginBiomass = targetVirginBiomass;
        this.targetVirginRecruits = targetVirginRecruits;
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
        int targetContainers = northBiologies.size() + southBiologies.size();
        int bycatchContainers = bycatchBios.size();
        double targetBiomass = targetVirginBiomass/targetContainers;
        double bycatchBiomass = bycatchVirginBiomass/bycatchContainers;


        for(BiomassLocalBiology bio : allBiologies)
        {
            bio.setCarryingCapacity(biology.getSpecie(0),targetBiomass);
            bio.setCurrentBiomass(biology.getSpecie(0),targetBiomass);
            if(bycatchBios.contains(bio))
            {
                bio.setCarryingCapacity(biology.getSpecie(1),bycatchBiomass);
                bio.setCurrentBiomass(biology.getSpecie(1),bycatchBiomass);
            }
        }




        //target grower
        //make sure we allocated the right amount of biomass
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCurrentBiomass()[0]).sum() - targetVirginBiomass) < FishStateUtilities.EPSILON;
        assert Math.abs(allBiologies.stream().mapToDouble(value -> value.getCarryingCapacity(0)).sum() - targetVirginBiomass) < FishStateUtilities.EPSILON;
        DerisoSchnuteCommonGrower targetGrower = new DerisoSchnuteCommonGrower(
                Collections.nCopies(targetRecruitmentLag,targetVirginBiomass),
                targetRho,
                targetNaturalSurvivalRate,
                targetRecruitmentSteepness,
                targetRecruitmentLag,
                0,
                targetWeightAtRecruitment,
                targetWeightAtRecruitmentMinus1,
                targetVirginRecruits
        );
        targetGrower.getBiologies().addAll(northBiologies);
        targetGrower.getBiologies().addAll(southBiologies);
        model.registerStartable(targetGrower);

        assert Math.abs(bycatchBios.stream().mapToDouble(value -> value.getCurrentBiomass()[1]).sum() - bycatchVirginBiomass) < FishStateUtilities.EPSILON;
        assert Math.abs(bycatchBios.stream().mapToDouble(value -> value.getCarryingCapacity(1)).sum() - bycatchVirginBiomass) < FishStateUtilities.EPSILON;
        //bycatch growers
        if(!separateBycatchStock)
        {
            //unified grower, very similar to the one for target fish

            DerisoSchnuteCommonGrower unifiedBycatchGrower = new DerisoSchnuteCommonGrower(
                    Collections.nCopies(
                            bycatchRecruitmentLag,bycatchVirginBiomass),
                    bycatchRho,
                    bycatchNaturalSurvivalRate,
                    bycatchRecruitmentSteepness,
                    bycatchRecruitmentLag,
                    1,
                    bycatchWeightAtRecruitment,
                    bycatchWeightAtRecruitmentMinus1,
                    bycatchVirginRecruits
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

            DerisoSchnuteCommonGrower northGrower = new DerisoSchnuteCommonGrower(
                    Collections.nCopies(
                            bycatchRecruitmentLag,virginNorth),
                    bycatchRho,
                    bycatchNaturalSurvivalRate,
                    bycatchRecruitmentSteepness,
                    bycatchRecruitmentLag,
                    1,
                    bycatchWeightAtRecruitment,
                    bycatchWeightAtRecruitmentMinus1,
                    bycatchVirginRecruits * proportionNorth
            );
            northGrower.getBiologies().addAll(northBiologies);
            model.registerStartable(northGrower);

            DerisoSchnuteCommonGrower southGrower = new DerisoSchnuteCommonGrower(
                    Collections.nCopies(
                            bycatchRecruitmentLag,bycatchVirginBiomass-virginNorth),
                    bycatchRho,
                    bycatchNaturalSurvivalRate,
                    bycatchRecruitmentSteepness,
                    bycatchRecruitmentLag,
                    1,
                    bycatchWeightAtRecruitment,
                    bycatchWeightAtRecruitmentMinus1,
                    bycatchVirginRecruits * (1d-proportionNorth)
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

    public double getBycatchVirginRecruits() {
        return bycatchVirginRecruits;
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

    public double getTargetVirginRecruits() {
        return targetVirginRecruits;
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
}
