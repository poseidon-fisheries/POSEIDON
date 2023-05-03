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
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * An aggregator of natural processes that applies recruitment, mortality
 * and aging to each fish
 * Created by carrknight on 3/7/16.
 */
public class SingleSpeciesNaturalProcesses implements Steppable, Startable
{



    /**
     * creates new recruits
     */
    private final RecruitmentProcess recruitment;


    /**
     * total recruits last step
     */
    private double lastRecruits = 0;

    private final Species species;


    private final boolean rounding;


    private final AgingProcess agingProcess;


    private final AbundanceDiffuser diffuser;


    private final NaturalMortalityProcess mortality;

    /**
     * if this is given the recruited biomass is distributed according to this table, otherwise it is distributed based
     * on where there is more biomass
     */
    private BiomassAllocator recruitsAllocator;

    final private boolean daily;


    public SingleSpeciesNaturalProcesses(
            RecruitmentProcess recruitment, Species species,
            boolean rounding, AgingProcess agingProcess, AbundanceDiffuser diffuser,
            NaturalMortalityProcess mortality, boolean daily) {
        this.species = species;
        this.recruitment = recruitment;
        this.rounding = rounding;
        this.agingProcess = agingProcess;
        this.diffuser = diffuser;
        this.mortality = mortality;
        this.daily = daily;
    }

    private final Map<SeaTile,AbundanceLocalBiology> biologies = new LinkedHashMap<>();

    /**
     * schedules itself every year
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {

        this.agingProcess.start(species);
        if(!daily) {
            model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
            model.scheduleEveryDay(new Steppable() {
                @Override
                public void step(SimState simState) {
                    diffuser.step(species, biologies, model);
                }
            }, StepOrder.BIOLOGY_PHASE);
        }
        else
        //will have to make sure diffuser and natural processes happen in sync;
        // in this case it means that the diffuser will act BEFORE the natural process, always
        {
            model.scheduleEveryDay(new Steppable() {
                @Override
                public void step(SimState simState) {
                    diffuser.step(species, biologies, model);
                    SingleSpeciesNaturalProcesses.this.step(simState);
                }
            }, StepOrder.BIOLOGY_PHASE);        }
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
        //we need to sum up all the abundances
        List<StructuredAbundance> abundances = new LinkedList<>();
        for (AbundanceLocalBiology biology : biologies.values()) {
            abundances.add(biology.getAbundance(species));
        }
        //now create the total number of recruits
        lastRecruits = recruitment.recruit(species, species.getMeristics(),
                                           StructuredAbundance.sum(abundances, abundances.get(0).getBins(),
                                                                   abundances.get(0).getSubdivisions()),
                                           model.getDayOfTheYear(),
                                           daysSimulated());
        if (rounding)
            lastRecruits = (int) (lastRecruits);
        abundances.clear();


        //decide where recruits are going to go before mortality hits
        //either allocate recruits with given allocator or proportional to where biomass is
        final LinkedHashMap<AbundanceLocalBiology, Double> biomassWeight = prepareAllocation(model);

        /***
         *      __  __         _        _ _ _
         *     |  \/  |___ _ _| |_ __ _| (_) |_ _  _
         *     | |\/| / _ \ '_|  _/ _` | | |  _| || |
         *     |_|  |_\___/_|  \__\__,_|_|_|\__|\_, |
         *                                      |__/
         */
        kill();


        /***
         *        _        _
         *       /_\  __ _(_)_ _  __ _
         *      / _ \/ _` | | ' \/ _` |
         *     /_/ \_\__, |_|_||_\__, |
         *           |___/       |___/
         */
        agingProcess.age(biologies.values(),
                         species,
                         model,
                         rounding,
                         daysSimulated());


        /***
         *        _   _ _              _   _
         *       /_\ | | |___  __ __ _| |_(_)___ _ _
         *      / _ \| | / _ \/ _/ _` |  _| / _ \ ' \
         *     /_/ \_\_|_\___/\__\__,_|\__|_\___/_||_|
         *
         */
        //allocate new recruits in a weighted fashion
        if(lastRecruits > 0) {
            allocate(biomassWeight);


        }
    }

    @NotNull
    private LinkedHashMap<AbundanceLocalBiology, Double> prepareAllocation(FishState model) {
        final LinkedHashMap<AbundanceLocalBiology, Double> biomassWeight= new LinkedHashMap<>();
        if (lastRecruits > 0) {
            if (recruitsAllocator != null) {

                double sum = 0;
                for (Map.Entry<SeaTile, AbundanceLocalBiology> entry : biologies.entrySet()) {
                    double weight = recruitsAllocator.allocate(entry.getKey(),
                                                               model.getMap(),
                                                               model.getRandom());
                    sum += weight;
                    biomassWeight.put(entry.getValue(), weight);

                }
                Preconditions.checkArgument(sum > 0, "No area valid for recruits!");
                for (AbundanceLocalBiology biology : biologies.values()) {
                    biomassWeight.replace(biology, biomassWeight.get(biology) / sum);
                    Preconditions.checkArgument(Double.isFinite(biomassWeight.get(biology)),
                                                "some weights are not finite");
                }

            }
            else {
                //map for each biology its total weight
                double totalBiomass = biologies.values().stream().mapToDouble(
                        value -> {
                            Double biomass = value.getBiomass(species);
                            biomassWeight.put(value, biomass);
                            return biomass;
                        }).sum();
                //reweight so they add up to 1
                for (AbundanceLocalBiology bio : biomassWeight.keySet())
                    biomassWeight.put(bio, biomassWeight.get(bio) / totalBiomass);


            }
        }
        return biomassWeight;
    }

    private void allocate(@NotNull LinkedHashMap<AbundanceLocalBiology, Double> biomassWeight) {
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
        for (Map.Entry<AbundanceLocalBiology, Double> biologyBiomass : biomassWeight.entrySet()) {
            double ratio = biologyBiomass.getValue();
            double recruitsHere = ((lastRecruits + leftOver) * ratio);
            StructuredAbundance abundance = biologyBiomass.getKey().getAbundance(species);

            if(rounding)
            {
                recruitsHere = (int) recruitsHere;
                int totalAllocated = 0;
                for(int subdivision = 0; subdivision <abundance.getSubdivisions(); subdivision++) {
                    abundance.asMatrix()[subdivision][0] += ((int) recruitsHere) / (abundance.getSubdivisions());
                    totalAllocated+= ((int) recruitsHere) / (abundance.getSubdivisions());
                }
                leftOver = ((lastRecruits + leftOver) * ratio) -totalAllocated;


            }
            else {
                for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {
                    abundance.asMatrix()[subdivision][0] += (recruitsHere) / ((double)abundance.getSubdivisions());

                }
            }

        }
    }

    private void kill() {
        for (AbundanceLocalBiology abundanceBasedLocalBiology : biologies.values()) {
            mortality.cull(
                    species.getMeristics(), rounding,
                    abundanceBasedLocalBiology.getAbundance(species), daysSimulated());
        }
    }

    private int daysSimulated() {
        return daily ? 1 : 365;
    }

    /**
     * register this biology so that it can be accessed by recruits and so on
     */
    public void add(AbundanceLocalBiology localBiology, SeaTile tile) {
        Preconditions.checkArgument(!biologies.containsKey(tile));
        Preconditions.checkArgument(!biologies.containsKey(localBiology));
        biologies.put(tile, localBiology);
    }



    public double getLastRecruits() {
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

    /**
     * Getter for property 'recruitment'.
     *
     * @return Value for property 'recruitment'.
     */
    public RecruitmentProcess getRecruitment() {
        return recruitment;
    }

    /**
     * Getter for property 'rounding'.
     *
     * @return Value for property 'rounding'.
     */
    public boolean isRounding() {
        return rounding;
    }

    /**
     * Getter for property 'agingProcess'.
     *
     * @return Value for property 'agingProcess'.
     */
    public AgingProcess getAgingProcess() {
        return agingProcess;
    }

    /**
     * Getter for property 'mortality'.
     *
     * @return Value for property 'mortality'.
     */
    public NaturalMortalityProcess getMortality() {
        return mortality;
    }

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }
}
