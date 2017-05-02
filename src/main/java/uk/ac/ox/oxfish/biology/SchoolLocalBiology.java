package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * Local biology that just asks "schools" if they are in the area
 * Created by carrknight on 11/17/16.
 */
public class SchoolLocalBiology extends AbstractBiomassBasedBiology {


    /**
     * list of all the schools of fish
     */
    private final List<InfiniteSchool> schools;

    /**
     * the position of this local biology
     */
    private final SeaTile position;


    public SchoolLocalBiology(List<InfiniteSchool> schools, SeaTile position) {
        this.schools = schools;
        this.position = position;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {

        double sum = 0;
        for(InfiniteSchool school : schools)
            if(school.getSpecies().equals(species) && school.contains(position))
                sum+=school.getBiomassPerCell();
        return sum;
    }

    /**
     * ignored
     * @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {

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
}
