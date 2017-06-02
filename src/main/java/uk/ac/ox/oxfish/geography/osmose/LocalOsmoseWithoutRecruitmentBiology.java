package uk.ac.ox.oxfish.geography.osmose;

import com.google.common.base.Preconditions;
import fr.ird.osmose.School;
import uk.ac.ox.ouce.oxfish.ExogenousMortality;
import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.oxfish.biology.AbstractBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
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

    /**
     * this multiplies the biology weight as stored into OSMOSE before returning it to the model.
     * This is useful because OSMOSE stores weight in tonnes while we want them in kilos.
     */
    private final double scalingFactor;

    /**
     * mortality from discard. So say .6 means that 60% of all fish discarded will die
     */
    private final double[] discardMortality;

    public LocalOsmoseWithoutRecruitmentBiology(
            ExogenousMortality mortality, CellBiomass counter,
            final double scalingFactor,
            final int[] recruitmentAge,
            final double[] discardMortality)
    {
        this.counter = counter;
        this.mortality = mortality;
        biomassFishedFromSchool = new HashMap<>();
        this.scalingFactor = scalingFactor;
        this.recruitmentAge= recruitmentAge;
        this.discardMortality = discardMortality;
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

        /*
        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species.getIndex());

        //count them all
        double currentBiomass = 0d;
        for(School school : schools) {
            final Double depletion = biomassFishedFromSchool.getOrDefault(school,0d);
            double biomass = school.getInstantaneousBiomass();
            assert biomass >= depletion;
            currentBiomass += biomass - depletion;

        }
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        assert  currentBiomass * scalingFactor >= getBiomass(species); //this includes juveniles so it should be more

        assert  counter.getBiomassOfSpecie(species.getIndex()) -
                biomassFishedFromSchool.values().stream()
                        .mapToDouble(Double::doubleValue).sum() == currentBiomass;
        return currentBiomass * scalingFactor;

*/
        return getBiomassIncludingJuveniles(species.getIndex());


    }

    protected double getBiomassIncludingJuveniles(int speciesIndex) {
        double currentBiomass =   counter.getBiomassOfSpecie(speciesIndex);
        for (Map.Entry<School, Double> pair : biomassFishedFromSchool.entrySet()) {

            if(pair.getKey().getSpecies().getIndex() == speciesIndex)
                currentBiomass -= pair.getValue();
        }


        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        assert  currentBiomass * scalingFactor >= getBiomass(speciesIndex) - FishStateUtilities.EPSILON
                : "biomass with juveniles " + currentBiomass * scalingFactor + ", biomass without juveniles " + getBiomass(speciesIndex);
        ; //this includes juveniles so it should be more
        //if recruitment mortality is 0, these two ought to be equal
        // A => B is equivalent to  (!A or B)
        assert  !(recruitmentAge[speciesIndex]  == 0) || (currentBiomass * scalingFactor == getBiomass(speciesIndex));
        return currentBiomass * scalingFactor;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    private Double getBiomass(int species) {
        return getBiomass(species,this.recruitmentAge[species],Integer.MAX_VALUE);

    }


    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *  @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {



        Preconditions.checkArgument(!caught.hasAbundanceInformation(),"Osmose biology isn't supposed to deal with abudance gear. What's going on?");

        for(int species = 0; species<caught.numberOfSpecies(); species++)
        {
            catchThisSpeciesByThisAmountWithinTheseAgeBounds(caught, notDiscarded, species,
                                                             this.recruitmentAge[species], Integer.MAX_VALUE);


        }
    }

    /**
     * catch this species given the weight and the min/max age (bounds included) you are catching
     * @param caught catch
     * @param notDiscarded catch not thrown back at sea
     * @param species species index
     * @param minAge
     * @param maxAge
     */
    public void catchThisSpeciesByThisAmountWithinTheseAgeBounds(
            Catch caught, Catch notDiscarded, int species, final int minAge,
            int maxAge) {
        //tokill = catches - surviving discards
        double biomassToKill = caught.getWeightCaught(species); //catches

        //if nothing was fished, then ignore
        if (biomassToKill < FishStateUtilities.EPSILON)
            return;


        double discards = biomassToKill - notDiscarded.getWeightCaught(species); //discards
        //make discards survive!
        biomassToKill -= discards * (1d-discardMortality[species]);
        assert biomassToKill>=0;
        assert discards>=0;

        //if nothing was killed, then ignore
        if (biomassToKill < FishStateUtilities.EPSILON)
            return;


        //with this getter we get biomass already scaled to kg
        double biomassAvailable = getBiomass(species,minAge,maxAge);
        assert biomassAvailable >= getBiomassIncludingJuveniles(species);

        if (biomassAvailable < FishStateUtilities.EPSILON) //if there is no fish, do not bother
            return;

        double proportionFishedPerEachSchool =biomassToKill / biomassAvailable;

        assert proportionFishedPerEachSchool >= 0;
        assert (biomassToKill) / (biomassAvailable+ FishStateUtilities.EPSILON) <= 1 : biomassToKill + " --- " + biomassAvailable; //rounding!
        proportionFishedPerEachSchool = Math.max(0,proportionFishedPerEachSchool);
        proportionFishedPerEachSchool = Math.min(1d,proportionFishedPerEachSchool);

        //you can't fish MORE than what is available right now
        Preconditions.checkArgument(biomassToKill <= biomassAvailable + FishStateUtilities.EPSILON,
                                    "can't fish this much, biomassToKill" + biomassToKill + " biomassAvailable: " + biomassAvailable);

        assert biomassAvailable >= 0;


        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(species);

        //if I sum up all the biomass from the list of school it should be equal to the biomassAvailable minus what we have caught already
        //variable I have (accounting for scaling!)
        assert Math.abs(schools.stream().filter(school -> school.getAge() >= minAge && school.getAge() <= maxAge)
                                .mapToDouble(School::getInstantaneousBiomass).sum() -
                biomassFishedFromSchool.entrySet().stream().filter(
                        pair -> schools.contains(pair.getKey())).mapToDouble(value -> value.getValue()).sum()
                                - biomassAvailable/scalingFactor)
                < FishStateUtilities.EPSILON : "school stream " +schools.stream().filter(school -> school.getAge() >= minAge && school.getAge() <= maxAge)
                                                                                                      .mapToDouble(School::getInstantaneousBiomass).sum() +
                " biomass available" + biomassAvailable/scalingFactor + ", already caught" +
                biomassFishedFromSchool.entrySet().stream().filter(
                        pair -> schools.contains(pair.getKey())).mapToDouble(value -> value.getValue()).sum();


        //go through each school
        //fish each with the same proportion
        double totalFished = 0;
        for (School school : schools) {

            //ignore small fish
            if (!(school.getAge() >= minAge && school.getAge() <= maxAge))
                continue;

            biomassFishedFromSchool.putIfAbsent(school, 0d);
            //count what has already been depleted
            final Double schoolEarlierDepletion = biomassFishedFromSchool.get(school);
            //fish the right proportion
            double currentLocalBiomass = school.getInstantaneousBiomass() - schoolEarlierDepletion;
            assert currentLocalBiomass >= 0;
            double fishedHere = currentLocalBiomass * proportionFishedPerEachSchool;
            assert schoolEarlierDepletion + fishedHere < school.getInstantaneousBiomass() + FishStateUtilities.EPSILON; //never fish more than possible (rounding errors)
            //make sure further rounding errors do not push you into catching more than the target
            assert fishedHere <= biomassToKill + FishStateUtilities.EPSILON : "fished here: " + fishedHere  +" , biomass to Kill" + biomassToKill;
            fishedHere = Math.min(Math.min(fishedHere,currentLocalBiomass),biomassToKill-totalFished);
            //rounding can push you into negative territory which we don't like

            assert fishedHere<=biomassToKill-totalFished;
            totalFished += fishedHere;
            assert totalFished<=biomassToKill + FishStateUtilities.EPSILON : biomassToKill + " , total fished " + totalFished + ", fished here: " + fishedHere;
            //should be no more than what we want to fish
            assert fishedHere <= biomassToKill : "fished here: " + fishedHere  +" , biomass to Kill" + biomassToKill;
            //should be positive or 0
            assert fishedHere >=-FishStateUtilities.EPSILON;
            fishedHere = Math.max(0,fishedHere);
            assert fishedHere >= 0 : "fished here: " + fishedHere;

            //register the catch
            //with the school

            biomassFishedFromSchool.put(school, Math.min(schoolEarlierDepletion + fishedHere,school.getInstantaneousBiomass()));
            assert biomassFishedFromSchool.get(school) >=0;
            //with the OSMOSE module
            mortality.incrementCatches(school, fishedHere);

        }
        assert Math.abs(totalFished - biomassToKill / scalingFactor) < FishStateUtilities.EPSILON;
        assert getBiomass(species)>=0;
    }


    /**
     * returns biomass but only within the bounds (both bounds are included)
     * @param speciesIndex the species index
     * @param minAge the minimum age of the school
     * @param maxAge the maximum age of the school
     * @return
     */
    public double getBiomass(int speciesIndex, int minAge, int maxAge)
    {
        //get all the schools of fish that belong to this species
        List<School> schools = counter.getSchoolsPerSpecie(speciesIndex);

        //count only  the biomass of the schools whose age is above (or equal to) recruitment age
        double currentBiomass = 0d;
        for(School school : schools) {
            final Double depletion = biomassFishedFromSchool.getOrDefault(school,0d);
            if(school.getAge() >= minAge && school.getAge() <= maxAge) {
                double biomass = school.getInstantaneousBiomass();
                assert biomass >= depletion;
                currentBiomass += biomass - depletion;
            }
        }
        assert  currentBiomass >= -FishStateUtilities.EPSILON;
        return scalingFactor * currentBiomass;
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