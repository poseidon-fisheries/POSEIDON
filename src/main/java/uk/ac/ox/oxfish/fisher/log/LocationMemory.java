package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.geography.SeaTile;

public class LocationMemory<T>
{
    private final SeaTile spot;

    private final T information;

    private int memoryAge;

    public LocationMemory(SeaTile spot, T information) {
        this.spot = spot;
        this.information = information;
        memoryAge = 0;
    }

    public SeaTile getSpot() {
        return spot;
    }

    public T getInformation() {
        return information;
    }

    public int getMemoryAge() {
        return memoryAge;
    }

    public int age(){
        return ++memoryAge;
    }
}
