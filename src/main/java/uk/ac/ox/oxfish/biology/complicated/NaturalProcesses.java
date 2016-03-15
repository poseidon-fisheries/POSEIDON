package uk.ac.ox.oxfish.biology.complicated;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * An aggregator of natural processes that applies recruitment, mortality
 * and aging to each fish
 * Created by carrknight on 3/7/16.
 */
public class NaturalProcesses implements Steppable, Startable
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

    public NaturalProcesses(
            NaturalMortalityProcess mortalityProcess,
            RecruitmentProcess recruitment) {
        this.mortalityProcess = mortalityProcess;
        this.recruitment = recruitment;
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
        for(final Species species : model.getSpecies())
        {
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
            //compute where there is more biomass
            HashMap<AbundanceBasedLocalBiology,Double> biomassWeight = new HashMap<>();
            double totalBiomass = biologies.stream().mapToDouble(
                    value -> {
                        Double biomass = value.getBiomass(species);
                        biomassWeight.put(value, biomass);
                        return biomass;
                    }).sum();

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
                    System.arraycopy(males,0,males,1,males.length-1);
                    System.arraycopy(females,0,females,1,females.length-1);
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
            biomassWeight.entrySet().parallelStream().forEach(
                    biologyBiomass -> {
                        assert biologyBiomass.getValue() <=totalBiomass;
                        double ratio = FishStateUtilities.round5(biologyBiomass.getValue() / totalBiomass);
                        int recruitsHere = (int) (lastRecruits * ratio);
                        biologyBiomass.getKey().getNumberOfFemaleFishPerAge(species)[0] = recruitsHere/2;
                        biologyBiomass.getKey().getNumberOfMaleFishPerAge(species)[0] = recruitsHere/2;
                    });



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
}
