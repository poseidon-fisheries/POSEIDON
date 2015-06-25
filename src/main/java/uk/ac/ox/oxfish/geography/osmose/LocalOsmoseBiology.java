package uk.ac.ox.oxfish.geography.osmose;

import uk.ac.ox.ouce.oxfish.cell.CellBiomass;
import uk.ac.ox.ouce.oxfish.cell.CellBiomassCounter;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The local biology object that links up with the OSMOSE cell
 * Created by carrknight on 6/25/15.
 */
public class LocalOsmoseBiology implements LocalBiology
{

    private final int x;

    private final int y;

    private final CellBiomass counter;

    public LocalOsmoseBiology(int x, int y, CellBiomass counter)
    {
        this.x = x;
        this.y = y;
        this.counter = counter;
    }

    /**
     * the biomass at this location for a single specie.
     *
     * @param specie the specie you care about
     * @return the biomass of this specie
     */
    @Override
    public Double getBiomass(Specie specie) {

        return counter.getBiomass(specie.getIndex());


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
}
