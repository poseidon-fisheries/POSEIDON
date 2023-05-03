package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * memorizes abundance and then redistributes according to given allocator
 * it according to the given allocators
 */
public class AbundanceResetter implements BiologyResetter {

    private BiomassAllocator allocator;

    private final Species species;

    private double[][] recordedAbundance;


    public AbundanceResetter(BiomassAllocator allocator, Species species) {
        this.allocator = allocator;
        this.species = species;
    }



    @Override
    public void recordHowMuchBiomassThereIs(FishState state)
    {
        recordedAbundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (SeaTile seaTile : state.getMap().getAllSeaTilesExcludingLandAsList())
        {

            if(!seaTile.isFishingEvenPossibleHere())
                continue;
            StructuredAbundance abundance = seaTile.getAbundance(species);
            for(int i=0; i<species.getNumberOfSubdivisions(); i++)
            {
                for (int j = 0; j < species.getNumberOfBins(); j++)
                {
                    recordedAbundance[i][j] += abundance.asMatrix()[i][j];
                }
            }

        }

    }



    public void resetAbundanceHere(SeaTile tile,
                                   NauticalMap map,
                                   MersenneTwisterFast random){

        if(!tile.isFishingEvenPossibleHere())
        {
            Preconditions.checkArgument(allocator.allocate(tile,map,random)==0 |
                            Double.isNaN(allocator.allocate(tile,map,random)),
                    "Allocating biomass on previously unfishable areas is not allowed; " +
                            "keep them empty but don't use always empty local biologies " + "\n" +
                            allocator.allocate(tile,map,random));
            return;
        }

        double[][] abundanceHere = tile.getAbundance(species).asMatrix();
        assert abundanceHere.length == species.getNumberOfSubdivisions();
        assert abundanceHere[0].length == species.getNumberOfBins();
        double weightHere = allocator.allocate(tile, map, random);

        for(int i=0; i<species.getNumberOfSubdivisions(); i++) {
            for (int j = 0; j < species.getNumberOfBins(); j++) {
                abundanceHere[i][j] = weightHere * recordedAbundance[i][j];
            }
        }


    }



    @Override
    public void resetAbundance(
            NauticalMap map,
            MersenneTwisterFast random)
    {
        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            resetAbundanceHere(seaTile,map,random);
        }
    }

    public BiomassAllocator getAllocator() {
        return allocator;
    }

    public void setAllocator(BiomassAllocator allocator) {
        this.allocator = allocator;
    }

    public Species getSpecies() {
        return species;
    }
}
