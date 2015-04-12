package uk.ac.ox.oxfish.biology;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The biology object containing general model-wise information like what species are modeled
 * Created by carrknight on 4/11/15.
 */
public class GlobalBiology
{

    /**
     * an unmodifiable list of species.
     */
    private final List<Specie> species;

    /**
     * for now all this has is a lot of species
     * @param species the species
     */
    public GlobalBiology(List<Specie> species)
    {
        this.species = Collections.unmodifiableList(species);
    }

    public GlobalBiology(Specie... species)
    {


        this.species = Collections.unmodifiableList(Arrays.asList(species));
    }
    /**
     *
     * @return an unmodifiable list of all the species available
     */
    public List<Specie> getSpecies() {
        return species;
    }
}
