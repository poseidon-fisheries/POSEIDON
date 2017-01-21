package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceBasedLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.SingleSpeciesNaturalProcesses;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

/**
 * The map initializer for the two species mpa vs itq example with abundance-driven
 * biology
 * Created by carrknight on 1/20/17.
 */
public class YellowBycatchInitializer implements BiologyInitializer {


    final private boolean separateYelloweyeStock;


    final private String targetSpeciesName;

    final private String bycatchSpeciesName;

    final private int initialTargetAbundance;

    final private int initialBycatchAbundance;

    final private double proportionJuvenileTarget;

    final private double proportionJuvenileBycatch;


    /**
     * any cell with x >= verticalSeparator will include the bycatch species
     */
    final private int verticalSeparator = 25;

    /**
     * seatiles that actually contain water
     */
    private HashMap<SeaTile,AbundanceBasedLocalBiology> tilesAtSea
            = new HashMap<>();

    /**
     * bycatch tiles
     */
    private List<AbundanceBasedLocalBiology> bycatchBios = new LinkedList<>();

    private List<AbundanceBasedLocalBiology> washingtonBios = new LinkedList<>();


    //todo move to factory
    public YellowBycatchInitializer() {
        separateYelloweyeStock = false;
        targetSpeciesName = "Sablefish";
        bycatchSpeciesName = "Yelloweye Rockfish";
        initialTargetAbundance = 100000000;
        initialBycatchAbundance = 10000000;
        proportionJuvenileTarget = .1;
        proportionJuvenileBycatch = .1;
    }

    public YellowBycatchInitializer(
            boolean separateYelloweyeStock,
            String targetSpeciesName,
            String bycatchSpeciesName,
            int initialTargetAbundance,
            int initialBycatchAbundance, double proportionJuvenileTarget, double proportionJuvenileBycatch) {
        this.separateYelloweyeStock = separateYelloweyeStock;
        this.targetSpeciesName = targetSpeciesName;
        this.bycatchSpeciesName = bycatchSpeciesName;
        this.initialTargetAbundance = initialTargetAbundance;
        this.initialBycatchAbundance = initialBycatchAbundance;
        this.proportionJuvenileTarget = proportionJuvenileTarget;
        this.proportionJuvenileBycatch = proportionJuvenileBycatch;
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
        AbundanceBasedLocalBiology bio = new AbundanceBasedLocalBiology(biology);

        //will it contain the bycatch?
        if(seaTile.getGridX()>=verticalSeparator) {
            bycatchBios.add(bio);
            if(seaTile.getGridY()<=mapHeightInCells/2)
                washingtonBios.add(bio);
        }
        //it will contain target since that's everywhere
        tilesAtSea.put(seaTile,bio);

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


        int targetCountPerTile = initialTargetAbundance / tilesAtSea.size();
        int bycatchCountPerTile = initialBycatchAbundance/  bycatchBios.size();
        Species target = biology.getSpecie(targetSpeciesName);
        assert target.getMaxAge()==1;
        assert getProportionJuvenileTarget() >=0;
        assert getProportionJuvenileTarget() <=1;


        Species bycatch = biology.getSpecie(bycatchSpeciesName);
        assert bycatch.getMaxAge() ==1;
        assert getProportionJuvenileBycatch() >=0;
        assert getProportionJuvenileBycatch() <=1;


        //this catches them all
        for(Map.Entry<SeaTile,AbundanceBasedLocalBiology> entry : tilesAtSea.entrySet())
        {
            SeaTile tile = entry.getKey();
            AbundanceBasedLocalBiology  local = entry.getValue();

            //start with target
            int juveniles = (int)(targetCountPerTile * getProportionJuvenileTarget());
            int adults = Math.max(targetCountPerTile - juveniles,0);
            local.getNumberOfMaleFishPerAge(target)[0] = juveniles;
            local.getNumberOfMaleFishPerAge(target)[1] = adults;


            //do the bycatch if needed
            if(tile.getGridX()>=verticalSeparator)
            {
                assert bycatchBios.contains(tile);
                juveniles = (int) (bycatchCountPerTile * getProportionJuvenileBycatch());
                adults = Math.max(0, bycatchCountPerTile - juveniles);
                local.getNumberOfMaleFishPerAge(bycatch)[0] = juveniles;
                local.getNumberOfMaleFishPerAge(bycatch)[1] = adults;
            }


        }


        //add natural process
        //target
        buildNaturalProcess(model, target, tilesAtSea.values());


        if(separateYelloweyeStock)
        {

            List<AbundanceBasedLocalBiology> southBios =
                    new LinkedList<>(bycatchBios);
            southBios.removeAll(washingtonBios);

            //separate natural
            buildNaturalProcess(model, bycatch, washingtonBios);
            buildNaturalProcess(model, bycatch, southBios);


        }
        else {
            //single unified natural process
            buildNaturalProcess(model, bycatch, bycatchBios);
        }




    }

    public void buildNaturalProcess(
            FishState model, Species target,
            final Collection<AbundanceBasedLocalBiology> bioList) {

        SingleSpeciesNaturalProcesses targetProcess =
                SingleSpeciesAbundanceInitializer.initializeNaturalProcesses(
                        model,
                        target,
                        bioList,
                        true,
                        0
                );
        //they redistribute uniformly in the area (replenish)
        HashMap<AbundanceBasedLocalBiology,Double> weight =
                new HashMap<>(bioList.size());
        for(AbundanceBasedLocalBiology bio : bioList)
            weight.put(bio,1d/ bioList.size());
        targetProcess.setFixedRecruitmentWeight(weight);
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


    /**
     * Getter for property 'separateYelloweyeStock'.
     *
     * @return Value for property 'separateYelloweyeStock'.
     */
    public boolean isSeparateYelloweyeStock() {
        return separateYelloweyeStock;
    }

    /**
     * Getter for property 'targetSpeciesName'.
     *
     * @return Value for property 'targetSpeciesName'.
     */
    public String getTargetSpeciesName() {
        return targetSpeciesName;
    }

    /**
     * Getter for property 'bycatchSpeciesName'.
     *
     * @return Value for property 'bycatchSpeciesName'.
     */
    public String getBycatchSpeciesName() {
        return bycatchSpeciesName;
    }


    /**
     * Getter for property 'initialTargetAbundance'.
     *
     * @return Value for property 'initialTargetAbundance'.
     */
    public int getInitialTargetAbundance() {
        return initialTargetAbundance;
    }

    /**
     * Getter for property 'initialBycatchAbundance'.
     *
     * @return Value for property 'initialBycatchAbundance'.
     */
    public int getInitialBycatchAbundance() {
        return initialBycatchAbundance;
    }

    /**
     * Getter for property 'proportionJuvenileTarget'.
     *
     * @return Value for property 'proportionJuvenileTarget'.
     */
    public double getProportionJuvenileTarget() {
        return proportionJuvenileTarget;
    }

    /**
     * Getter for property 'proportionJuvenileBycatch'.
     *
     * @return Value for property 'proportionJuvenileBycatch'.
     */
    public double getProportionJuvenileBycatch() {
        return proportionJuvenileBycatch;
    }

    /**
     * Getter for property 'verticalSeparator'.
     *
     * @return Value for property 'verticalSeparator'.
     */
    public int getVerticalSeparator() {
        return verticalSeparator;
    }
}
