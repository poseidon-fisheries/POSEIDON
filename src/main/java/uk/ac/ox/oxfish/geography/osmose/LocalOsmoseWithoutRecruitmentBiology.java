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
 * A modified OSMOSE biology link that ignores all biomass from schools of fish below recruitment age
 * Created by carrknight on 2/15/17.
 */
public class LocalOsmoseWithoutRecruitmentBiology extends AbstractBiomassBasedBiology {


    /**
     * map containing recrtuiment ages
     */
    final private int[] recruitmentAge;


    private final CellBiomass counter;

    private final ExogenousMortality mortality;


    private final Map<School,Double> biomassFishedFromSchool;

    private final MersenneTwisterFast random;

    /**
     * this multiplies the biology weight as stored into OSMOSE before returning it to the model.
     * This is useful because OSMOSE stores weight in tonnes while we want them in kilos.
     */
    private final double scalingFactor;


    public LocalOsmoseWithoutRecruitmentBiology(
            ExogenousMortality mortality, CellBiomass counter,
            MersenneTwisterFast random,
            final double scalingFactor,
            final int[] recruitmentAge)
    {
        this.counter = counter;
        this.mortality = mortality;
        this.random = random;
        biomassFishedFromSchool = new HashMap<>();
        this.scalingFactor = scalingFactor;
        this.recruitmentAge= recruitmentAge;
    }


    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {

        return getBiomass(species.getIndex());

    }


    public double getBiomassIncludingJuveniles(Species species)
    {
        final double currentBiomass =
            counter.getBiomass(species.getIndex()) - biomassFishedFromSchool.getOrDefault(species.getIndex(),
                                                                                          0d);
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        return currentBiomass;

    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    private Double getBiomass(int species) {

        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species);
        Integer recruitmentAge = this.recruitmentAge[species];

        //count only  the biomass of the schools whose age is above (or equal to) recruitment age
        double currentBiomass = 0d;
        for(School school : schools) {
            final Double depletion = biomassFishedFromSchool.getOrDefault(school,0d);
            if(school.getAge() >= recruitmentAge) {
                double biomass = school.getInstantaneousBiomass();
                assert biomass >= depletion;
                currentBiomass += biomass - depletion;
            }
        }
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        return scalingFactor * currentBiomass;

    }


    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param species       the species fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {

        //if nothing was fished, then ignore
        if(biomassFished<FishStateUtilities.EPSILON)
            return;

        // do not need to scale since they are both "wrong" and all we care about is their proportion
        double biomassAvailable = getBiomass(species);

        if(biomassAvailable<FishStateUtilities.EPSILON) //if there is no fish, do not bother
            return;

        double proportionFishedPerEachSchool =
                        biomassFished/biomassAvailable;

        assert proportionFishedPerEachSchool >=0;
        assert proportionFishedPerEachSchool <=1;

        //you can't fish MORE than what is available right now
        Preconditions.checkArgument(biomassFished<=biomassAvailable+FishStateUtilities.EPSILON,
                                    "can't fish this much!");

        assert biomassAvailable >=0;


        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species.getIndex());

        //if I sum up all the biomass from the list of school it should be equal to the biomassAvailable
        //variable I have
        Integer recruitmentAge = this.recruitmentAge[species.getIndex()];
        assert Math.abs(schools.stream().filter(school -> school.getAge() >=recruitmentAge )
                                .mapToDouble(School::getInstantaneousBiomass).sum()
                                -biomassAvailable)
                < FishStateUtilities.EPSILON;



        //go through each school
        //fish each with the same proportion
        double totalFished = 0;
        for(School school : schools)
        {

            //ignore small fish
            if(school.getAge()<recruitmentAge)
                continue;

            biomassFishedFromSchool.putIfAbsent(school, 0d);
            //count what has already been depleted
            final Double schoolEarlierDepletion = biomassFishedFromSchool.get(school);
            //fish the right proportion
            double fishedHere = (school.getInstantaneousBiomass()- schoolEarlierDepletion) * proportionFishedPerEachSchool;
            totalFished += fishedHere;
            //should be no more than what we want to fish
            assert fishedHere <=biomassFished;
            //should be positive or 0
            assert fishedHere >=0;

            //register the catch
            //with the school
            biomassFishedFromSchool.put(school,schoolEarlierDepletion+fishedHere);
            //with the OSMOSE module
            mortality.incrementCatches(school,fishedHere);

        }
        assert  totalFished == biomassFished/scalingFactor;
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
        biomassFishedFromSchool.clear();
    }

    @Override
    public String toString() {

        double[] toPrint = new double[recruitmentAge.length];
        for(int i =0; i<toPrint.length; i++ )
            toPrint[i] = getBiomass(i);

        return Arrays.toString(toPrint);

    }
}


    /**
     *
     * old schooling algorithm. Might come useful later

    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {
        //scale
        biomassFished /= scalingFactor;
        double biomassAvailable = getBiomass(species) / scalingFactor;




        //you can't fish MORE than what is available right now
        Preconditions.checkArgument(biomassFished<=biomassAvailable+FishStateUtilities.EPSILON,
                                    "can't fish this much!");

        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species.getIndex());

        //if I sum up all the biomass from the list of school it should be equal to the biomassAvailable
        //variable I have
        Integer recruitmentAge = this.recruitmentAge[species.getIndex()];

        assert Math.abs(schools.stream().filter(school -> school.getAge() >=recruitmentAge )
                                .mapToDouble(School::getInstantaneousBiomass).sum()
                                -biomassAvailable)
                < FishStateUtilities.EPSILON;

        //shuffle the schools
        Collections.shuffle(schools, new Random(random.nextLong()));

        //go through each school
        final ListIterator<School> listIterator = schools.listIterator();
        //as long as there is something to fish
        double biomassToConsume = biomassFished;
        while(biomassToConsume > 0)
        {
            //pick next valid school
            School school;
            do {
                school = listIterator.next();
            }
            while(school.getAge()<recruitmentAge);

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
    }
     */