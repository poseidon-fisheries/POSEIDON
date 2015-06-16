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
    private final Specie species[];


    private final List<Specie> unmodifiableView;

    public GlobalBiology(Specie... species)
    {



        this.species = species;
        for(int i=0; i<species.length; i++) //now assign a number to each
            species[i].setIndex(i);
        unmodifiableView = Collections.unmodifiableList(Arrays.asList(species));
    }


    /**
     * instantiate a list of random species
     * @param numberOfSpecies the number of species
     */
    public static GlobalBiology genericListOfSpecies(int numberOfSpecies){
        Specie[] generics = new Specie[numberOfSpecies];
        for(int i=0; i<numberOfSpecies; i++)
            generics[i] = new Specie("Specie " + i);
        return new GlobalBiology(generics);
    }
    /**
     *
     * @return an unmodifiable list of all the species available
     */
    public List<Specie> getSpecies()
    {
        return unmodifiableView;
    }

    public Specie getSpecie(int order)
    {
        return species[order];
    }

    public int getSize()
    {
        return species.length;
    }

}
