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

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

/**
 * An aggregator of natural processes that applies recruitment, mortality
 * and aging to each fish
 * Created by carrknight on 3/7/16.
 */
public class SingleSpeciesNaturalProcesses implements Steppable, Startable
{

    /**
     * kills off fish
     */
    private final NaturalMortalityProcess mortalityProcess;

    /**
     * creates new recruits
     */
    private final RecruitmentProcess recruitment;


    /**
     * total recruits last step
     */
    private int lastRecruits = 0;

    private final Species species;



    private final AgingProcess agingProcess;


    private final AbundanceDiffuser diffuser;


    /**
     * if this is given the recruited biomass is distributed according to this table, otherwise it is distributed based
     * on where there is more biomass
     */
    private BiomassAllocator recruitsAllocator;

    public SingleSpeciesNaturalProcesses(
            NaturalMortalityProcess mortalityProcess,
            RecruitmentProcess recruitment, Species species,
            AgingProcess agingProcess, AbundanceDiffuser diffuser) {
        this.species = species;
        this.mortalityProcess = mortalityProcess;
        this.recruitment = recruitment;
        this.agingProcess = agingProcess;
        this.diffuser = diffuser;
    }

    private final Map<SeaTile,AbundanceBasedLocalBiology> biologies = new LinkedHashMap<>();

    /**
     * schedules itself every year
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {

        model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);

        model.scheduleEveryDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                diffuser.step(species,biologies,model);
            }
        }, StepOrder.BIOLOGY_PHASE);

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
    }

    /**
     *  Recruitment + Mortality + Aging + Allocation of new Recruits <br>
     *  New recruits are allocated proportional to the areas that have more biomass.
     *
     */
    @Override
    public void step(SimState simState) {

        FishState model = (FishState) simState;

        /***
         *      ___                 _ _                 _
         *     | _ \___ __ _ _ _  _(_) |_ _ __  ___ _ _| |_
         *     |   / -_) _| '_| || | |  _| '  \/ -_) ' \  _|
         *     |_|_\___\__|_|  \_,_|_|\__|_|_|_\___|_||_\__|
         *
         */
        //we need to sum up all the male/female
        int totalMale[] = new int[species.getMaxAge()+1];
        int totalFemale[] = new int[species.getMaxAge()+1];
        biologies.values().stream().forEach(abundanceBasedLocalBiology -> {
            int[] females = abundanceBasedLocalBiology.getNumberOfFemaleFishPerAge(species);
            int[] males = abundanceBasedLocalBiology.getNumberOfMaleFishPerAge(species);
            for(int age=0; age<totalMale.length; age++)
            {
                totalFemale[age] += females[age];
                totalMale[age] += males[age];
            }
        });
        //now create the total number of recruits
        lastRecruits = recruitment.recruit(species, species.getMeristics(),
                                           totalFemale, totalMale);


        //allocate stuff before mortality hits!
        //either allocate recruits with given allocator or proportional to where biomass is
        LinkedHashMap<AbundanceBasedLocalBiology,Double> biomassWeight;
        if(recruitsAllocator != null) {

            double sum = 0;
            biomassWeight = new LinkedHashMap<>();
            for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> entry : biologies.entrySet()) {
                double weight = recruitsAllocator.allocate(entry.getKey(),
                                                           model.getMap(),
                                                           model.getRandom());
                sum+=weight;
                biomassWeight.put(entry.getValue(), weight);

            }
            Preconditions.checkArgument(sum > 0,"No area valid for recruits!");
            for (AbundanceBasedLocalBiology biology : biologies.values()) {
                biomassWeight.replace(biology,biomassWeight.get(biology)/sum);
                Preconditions.checkArgument(Double.isFinite(biomassWeight.get(biology)),
                                            "some weights are not finite");
            }


        }
        else {
            biomassWeight = new LinkedHashMap<>();
            //map for each biology its total weight
            double totalBiomass = biologies.values().stream().mapToDouble(
                    value -> {
                        Double biomass = value.getBiomass(species);
                        biomassWeight.put(value, biomass);
                        return biomass;
                    }).sum();
            //reweight so they add up to 1
            for(AbundanceBasedLocalBiology bio : biomassWeight.keySet())
                biomassWeight.put(bio,biomassWeight.get(bio)/totalBiomass);


        }


        /***
         *      __  __         _        _ _ _
         *     |  \/  |___ _ _| |_ __ _| (_) |_ _  _
         *     | |\/| / _ \ '_|  _/ _` | | |  _| || |
         *     |_|  |_\___/_|  \__\__,_|_|_|\__|\_, |
         *                                      |__/
         */
        biologies.values().forEach(
                abundanceBasedLocalBiology -> mortalityProcess.cull(abundanceBasedLocalBiology.getNumberOfMaleFishPerAge(species),
                                                                    abundanceBasedLocalBiology.getNumberOfFemaleFishPerAge(species),
                                                                    species.getMeristics()));



        /***
         *        _        _
         *       /_\  __ _(_)_ _  __ _
         *      / _ \/ _` | | ' \/ _` |
         *     /_/ \_\__, |_|_||_\__, |
         *           |___/       |___/
         */
        biologies.values().forEach(new Consumer<AbundanceBasedLocalBiology>() {
            @Override
            public void accept(AbundanceBasedLocalBiology abundanceBasedLocalBiology) {
                agingProcess.ageLocally(abundanceBasedLocalBiology,species,model);
            }
        });


        /***
         *        _   _ _              _   _
         *       /_\ | | |___  __ __ _| |_(_)___ _ _
         *      / _ \| | / _ \/ _/ _` |  _| / _ \ ' \
         *     /_/ \_\_|_\___/\__\__,_|\__|_\___/_||_|
         *
         */
        //allocate new recruits in a weighted fashion
        if(lastRecruits > 0) {


            //make sure it all sum up to 1!
            assert Math.abs(biomassWeight.values().stream().
                    mapToDouble(
                            new ToDoubleFunction<Double>() {
                                @Override
                                public double applyAsDouble(Double value) {
                                    return value;
                                }
                            }).sum()-1d)<.001d;
            double leftOver = 0;
                for (Map.Entry<AbundanceBasedLocalBiology, Double> biologyBiomass : biomassWeight.entrySet()) {
                    double ratio = biologyBiomass.getValue();
                    int recruitsHere = (int) ((lastRecruits + leftOver) * ratio);
                    //add recruits to smallest bin
                    biologyBiomass.getKey().getNumberOfFemaleFishPerAge(species)[0] += recruitsHere / 2;
                    biologyBiomass.getKey().getNumberOfMaleFishPerAge(species)[0] += recruitsHere / 2;
                    leftOver = ((lastRecruits + leftOver) * ratio) -
                            biologyBiomass.getKey().getNumberOfFemaleFishPerAge(species)[0] -
                            biologyBiomass.getKey().getNumberOfMaleFishPerAge(species)[0]
                    ;
                }

        }
    }

    /**
     * register this biology so that it can be accessed by recruits and so on
     */
    public void add(AbundanceBasedLocalBiology localBiology, SeaTile tile) {
        Preconditions.checkArgument(!biologies.containsKey(tile));
        Preconditions.checkArgument(!biologies.containsKey(localBiology));
        biologies.put(tile, localBiology);
    }



    public int getLastRecruits() {
        return lastRecruits;
    }



    /**
     * give a function to generate noise as % of recruits this year
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    public void addNoise(NoiseMaker noiseMaker) {
        recruitment.addNoise(noiseMaker);
    }

    /**
     * Getter for property 'diffuser'.
     *
     * @return Value for property 'diffuser'.
     */
    public AbundanceDiffuser getDiffuser() {
        return diffuser;
    }


    /**
     * Getter for property 'recruitsAllocator'.
     *
     * @return Value for property 'recruitsAllocator'.
     */
    public BiomassAllocator getRecruitsAllocator() {
        return recruitsAllocator;
    }

    /**
     * Setter for property 'recruitsAllocator'.
     *
     * @param recruitsAllocator Value to set for property 'recruitsAllocator'.
     */
    public void setRecruitsAllocator(BiomassAllocator recruitsAllocator) {
        this.recruitsAllocator = recruitsAllocator;
    }

    private boolean randomRounding = true;

    /**
     * Getter for property 'randomRounding'.
     *
     * @return Value for property 'randomRounding'.
     */
    public boolean isRandomRounding() {
        return randomRounding;
    }

    /**
     * Setter for property 'randomRounding'.
     *
     * @param randomRounding Value to set for property 'randomRounding'.
     */
    public void setRandomRounding(boolean randomRounding) {
        this.randomRounding = randomRounding;
    }
}
