package uk.ac.ox.oxfish.biology;

/**
 * A collection of all information regarding a specie (for now just a name)
 * Created by carrknight on 4/11/15.
 */
public class Specie {

    private final String name;

    /**
     * the specie index, basically its order in the species array.
     */
    private int index;

    public Specie(String name) {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
