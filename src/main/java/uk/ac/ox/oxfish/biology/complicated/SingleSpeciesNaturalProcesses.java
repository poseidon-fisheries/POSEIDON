package uk.ac.ox.oxfish.biology.complicated;

import com.esotericsoftware.minlog.Log;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

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


    /**
     * when this is true mortality rate of the oldest class is 100%
     */
    private final boolean preserveLastAge;


    /**
     * if this is given the recruited biomass is distributed according to this table, otherwise it is distributed based
     * on where there is more biomass
     */
    private HashMap<AbundanceBasedLocalBiology,Double> fixedRecruitmentWeight;

    public SingleSpeciesNaturalProcesses(
            NaturalMortalityProcess mortalityProcess,
            RecruitmentProcess recruitment, Species species,
            boolean preserveLastAge) {
        this.species = species;
        this.mortalityProcess = mortalityProcess;
        this.recruitment = recruitment;
        this.preserveLastAge = preserveLastAge;
    }

    private final LinkedList<AbundanceBasedLocalBiology> biologies = new LinkedList<>();

    /**
     * schedules itself every year
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {
        model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
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
        biologies.stream().forEach(abundanceBasedLocalBiology -> {
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

        //either take the given recruitment weight or compute it as current weight
        HashMap<AbundanceBasedLocalBiology,Double> biomassWeight;
        if(fixedRecruitmentWeight != null) {
            if(Log.TRACE)
                Log.trace("using fixed recruitment weight, total weight(should be one): " + fixedRecruitmentWeight.values().
                        stream().mapToDouble(Double::doubleValue).sum());
            biomassWeight = fixedRecruitmentWeight;
        }
        else {
            biomassWeight = new HashMap<>();
            //map for each biology its total weight
            double totalBiomass = biologies.stream().mapToDouble(
                    value -> {
                        Double biomass = value.getBiomass(species);
                        biomassWeight.put(value, biomass);
                        return biomass;
                    }).sum();
            //reweight so they add up to 1
            for(AbundanceBasedLocalBiology bio : biomassWeight.keySet())
                biomassWeight.put(bio,FishStateUtilities.round5(biomassWeight.get(bio)/totalBiomass));
        }

        /***
         *      __  __         _        _ _ _
         *     |  \/  |___ _ _| |_ __ _| (_) |_ _  _
         *     | |\/| / _ \ '_|  _/ _` | | |  _| || |
         *     |_|  |_\___/_|  \__\__,_|_|_|\__|\_, |
         *                                      |__/
         */
        biologies.parallelStream().forEach(
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
        biologies.parallelStream().forEach(new Consumer<AbundanceBasedLocalBiology>() {
            @Override
            public void accept(AbundanceBasedLocalBiology abundanceBasedLocalBiology) {
                int[] males = abundanceBasedLocalBiology.getNumberOfMaleFishPerAge(species);
                int[] females = abundanceBasedLocalBiology.getNumberOfFemaleFishPerAge(species);

                int oldestMale = males[males.length-1];
                int oldestFemale = females[females.length-1];

                System.arraycopy(males,0,males,1,males.length-1);
                System.arraycopy(females,0,females,1,females.length-1);
                if(preserveLastAge)
                {
                    males[males.length - 1] += oldestMale;
                    females[females.length - 1] += oldestFemale;

                }
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
        double leftOver = 0;
        for (Map.Entry<AbundanceBasedLocalBiology, Double> biologyBiomass : biomassWeight.entrySet()) {
            double ratio = biologyBiomass.getValue();
            int recruitsHere = (int) ((lastRecruits+leftOver) * ratio);
            biologyBiomass.getKey().getNumberOfFemaleFishPerAge(species)[0] = recruitsHere / 2;
            biologyBiomass.getKey().getNumberOfMaleFishPerAge(species)[0] = recruitsHere / 2;
            leftOver =  ((lastRecruits+leftOver) * ratio) -
                    biologyBiomass.getKey().getNumberOfFemaleFishPerAge(species)[0] -
                    biologyBiomass.getKey().getNumberOfMaleFishPerAge(species)[0]
            ;
        }


    }

    /**
     * register this biology so that it can be accessed by recruits and so on
     */
    public boolean add(AbundanceBasedLocalBiology abundanceBasedLocalBiology) {
        return biologies.add(abundanceBasedLocalBiology);
    }



    public int getLastRecruits() {
        return lastRecruits;
    }

    /**
     * Getter for property 'fixedRecruitmentWeight'.
     *
     * @return Value for property 'fixedRecruitmentWeight'.
     */
    public HashMap<AbundanceBasedLocalBiology, Double> getFixedRecruitmentWeight() {
        return fixedRecruitmentWeight;
    }

    /**
     * Setter for property 'fixedRecruitmentWeight'.
     *
     * @param fixedRecruitmentWeight Value to set for property 'fixedRecruitmentWeight'.
     */
    public void setFixedRecruitmentWeight(
            HashMap<AbundanceBasedLocalBiology, Double> fixedRecruitmentWeight) {
        this.fixedRecruitmentWeight = fixedRecruitmentWeight;
    }

    /**
     * give a function to generate noise as % of recruits this year
     * @param noiseMaker the function that generates percentage changes. 1 means no noise.
     */
    public void addNoise(NoiseMaker noiseMaker) {
        recruitment.addNoise(noiseMaker);
    }
}
