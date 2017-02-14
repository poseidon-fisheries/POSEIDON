package uk.ac.ox.oxfish.geography.osmose;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import fr.ird.osmose.School;
import uk.ac.ox.ouce.oxfish.ExogenousMortality;
import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.oxfish.biology.AbstractBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 * The local biology object that links up with the OSMOSE cell
 * Created by carrknight on 6/25/15.
 */
public class LocalOsmoseByBiomassBiology extends AbstractBiomassBasedBiology
{

    private final CellBiomass counter;

    private final ExogenousMortality mortality;

    private final double[] biomassAlreadyFished;

    private final Map<School,Double> biomassFishedFromSchool;

    private final MersenneTwisterFast random;

    /**
     * this multiplies the biology weight as stored into OSMOSE before returning it to the model.
     * This is useful because OSMOSE stores weight in tonnes while we want them in kilos.
     */
    private final double scalingFactor;

    public LocalOsmoseByBiomassBiology(
            ExogenousMortality mortality, CellBiomass counter,
            int numberOfSpecies, MersenneTwisterFast random,
            double scalingFactor)
    {
        this.counter = counter;
        this.mortality = mortality;
        this.random = random;
        biomassAlreadyFished = new double[numberOfSpecies];
        biomassFishedFromSchool = new HashMap<>();
        this.scalingFactor = scalingFactor;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {

        final double currentBiomass =
                counter.getBiomass(species.getIndex()) - biomassAlreadyFished[species.getIndex()];
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        return scalingFactor * currentBiomass;




    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param species        the species fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished)
    {
        biomassFished /= scalingFactor;
        //this is the biomass available for this species
        double biomassAvailable = counter.getBiomass(species.getIndex())-
                biomassAlreadyFished[species.getIndex()] ;
        //you can't fish MORE than what is available right now
        Preconditions.checkArgument(biomassFished<=biomassAvailable+FishStateUtilities.EPSILON,
                                    "can't fish this much!");

        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species.getIndex());

        //if I sum up all the biomass from the list of school it should be equal to the biomassAvailable
        //variable I have
        assert Math.abs(schools.stream().mapToDouble(School::getInstantaneousBiomass).sum()
                                -biomassAvailable-biomassAlreadyFished[species.getIndex()])
                                < FishStateUtilities.EPSILON;

        //shuffle the schools
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
            mortality.incrementCatches(school,fishedHere);

        }



        //count the biomass as fished!
        biomassAlreadyFished[species.getIndex()]+=biomassFished;

    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff() {

    }

    public void osmoseStep(){
        Arrays.fill(biomassAlreadyFished,0d);
        biomassFishedFromSchool.clear();
    }

    @Override
    public String toString() {

        double[] toPrint = new double[biomassAlreadyFished.length];
        for(int i =0; i<toPrint.length; i++ )
            toPrint[i] = (counter.getBiomass(i) - biomassAlreadyFished[i])*scalingFactor;

        return Arrays.toString(toPrint);

    }
}
