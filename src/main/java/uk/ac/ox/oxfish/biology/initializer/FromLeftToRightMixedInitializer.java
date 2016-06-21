package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.ConstantHeterogeneousLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * From left to right, two species, well-mixed
 * Created by carrknight on 6/20/16.
 */
public class FromLeftToRightMixedInitializer extends AbstractBiologyInitializer {

    final private double proportionSecondSpeciesToFirst;

    /**
     * leftmost biomass
     */
    final private double maximumBiomass;




    private String firstSpeciesName = "Species 0";


    private String secondSpeciesName = "Species 1";



    public FromLeftToRightMixedInitializer(
            double maximumBiomass,
            double proportionSecondSpeciesToFirst) {
        this.proportionSecondSpeciesToFirst = proportionSecondSpeciesToFirst;
        this.maximumBiomass = maximumBiomass;
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {
        if (seaTile.getAltitude() > 0)
            return new EmptyLocalBiology();
        else {
            double correctBiomass = maximumBiomass*
                    Math.pow((1-seaTile.getGridX()/(double)mapWidthInCells)
                            ,2);

            return new ConstantHeterogeneousLocalBiology(correctBiomass,
                                                         correctBiomass*
                                                                 proportionSecondSpeciesToFirst);
        }
    }


    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology,
            NauticalMap map,
            MersenneTwisterFast random,
            FishState model) {

    }

    /**
     * "Species 0" and "Species 1"
     *
     * @return the name of the species
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{
                firstSpeciesName,
                secondSpeciesName};
    }


    public String getFirstSpeciesName() {
        return firstSpeciesName;
    }

    public void setFirstSpeciesName(String firstSpeciesName) {
        this.firstSpeciesName = firstSpeciesName;
    }

    public String getSecondSpeciesName() {
        return secondSpeciesName;
    }

    public void setSecondSpeciesName(String secondSpeciesName) {
        this.secondSpeciesName = secondSpeciesName;
    }

    /**
     * Getter for property 'proportionSecondSpeciesToFirst'.
     *
     * @return Value for property 'proportionSecondSpeciesToFirst'.
     */
    public double getProportionSecondSpeciesToFirst() {
        return proportionSecondSpeciesToFirst;
    }

    /**
     * Getter for property 'maximumBiomass'.
     *
     * @return Value for property 'maximumBiomass'.
     */
    public double getMaximumBiomass() {
        return maximumBiomass;
    }
}
