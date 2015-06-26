package uk.ac.ox.oxfish.geography.osmose;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import fr.ird.osmose.School;
import uk.ac.ox.ouce.oxfish.ExogenousMortality;
import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * The local biology object that links up with the OSMOSE cell
 * Created by carrknight on 6/25/15.
 */
public class LocalOsmoseBiology implements LocalBiology
{

    private final CellBiomass counter;

    private final ExogenousMortality mortality;

    private final double[] biomassAlreadyFished;

    private final Map<School,Double> biomassFishedFromSchool;

    private final MersenneTwisterFast random;

    public LocalOsmoseBiology(
            ExogenousMortality mortality, CellBiomass counter,
            int numberOfSpecies, MersenneTwisterFast random)
    {
        this.counter = counter;
        this.mortality = mortality;
        this.random = random;
        biomassAlreadyFished = new double[numberOfSpecies];
        biomassFishedFromSchool = new HashMap<>();
    }

    /**
     * the biomass at this location for a single specie.
     *
     * @param specie the specie you care about
     * @return the biomass of this specie
     */
    @Override
    public Double getBiomass(Specie specie) {

        final double currentBiomass = counter.getBiomass(specie.getIndex()) - biomassAlreadyFished[specie.getIndex()];
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        return currentBiomass;




    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param specie        the specie fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Specie specie, Double biomassFished)
    {
        //this is the biomass available for this specie
        double biomassAvailable = counter.getBiomass(specie.getIndex())-
                biomassAlreadyFished[specie.getIndex()] ;
        //you can't fish MORE than what is available right now
        Preconditions.checkArgument(biomassAvailable-biomassFished>FishStateUtilities.EPSILON,
                                    "can't fish this much!");

        //get all the schools of fish that belong to this specie
        List<School> schools = counter.getSchoolsPerSpecie(specie.getIndex());

        //if I sum up all the biomass from the list of school it should be equal to the biomassAvailable
        //variable I have
        assert Math.abs(schools.stream().mapToDouble(School::getInstantaneousBiomass).sum()
                                -biomassAvailable-biomassAlreadyFished[specie.getIndex()])
                                < FishStateUtilities.EPSILON;

        //shuffle the school
        Collections.shuffle(schools,new Random(random.nextLong()));

        //go through each school
        final ListIterator<School> listIterator = schools.listIterator();
        //as long as there is something to fish
        double biomassToConsume = biomassFished;
        while(biomassToConsume > 0)
        {
            //for this school
            final School school = listIterator.next();
            biomassFishedFromSchool.putIfAbsent(school, 0d);
            //count what has already been depleted
            final Double schoolEarlierDepletion = biomassFishedFromSchool.get(school);
            //fish as much as you can
            double fishedHere = Math.max(0,
                                         Math.min(school.getInstantaneousBiomass()- schoolEarlierDepletion,
                                                  biomassToConsume));

            //should be no more than what we want to fish
            assert fishedHere <=biomassToConsume;
            assert fishedHere <=biomassFished;
            //should be positive or 0
            assert fishedHere >=0;

            //register the catch
            //with yourself
            biomassToConsume-=biomassFished;
            //with the school
            biomassFishedFromSchool.put(school,schoolEarlierDepletion+fishedHere);
            //with the OSMOSE module
            mortality.incrementCatches(school,fishedHere);;

        }



        //count the biomass as fished!
        biomassAlreadyFished[specie.getIndex()]+=biomassFished;

    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    public void osmoseStep(){
        Arrays.fill(biomassAlreadyFished,0d);
        biomassFishedFromSchool.clear();
    }
}
