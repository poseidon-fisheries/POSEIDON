package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-generates weighted maps, normalises if needed and in
 * general takes care of grudge
 * Created by carrknight on 6/30/17.
 */
public class AllocatorManager {

    /**
     * whether we should make weights sum up to one
     */
    final private boolean normalize;

    /**
     * created weight maps from seatile to weight for each species
     */
    final private Map<Species,Map<SeaTile,Double>> weightMaps;

    /**
     * list of allocators, one for each species
     */
    final private List<BiomassAllocator> allocators;


    public AllocatorManager(
            boolean normalize,
            List<BiomassAllocator> allocators,
            GlobalBiology biology) {
        this.normalize = normalize;
        this.allocators = allocators;
        Preconditions.checkArgument(biology.getSize()==allocators.size(),
                                    "size mismatch between # of species and # of allocators");

        weightMaps = new HashMap<>(biology.getSpecies().size());
        //prepare the maps
        for(Species species : biology.getSpecies())
            weightMaps.put(species,new HashMap<>());

    }


    public void start(NauticalMap map, MersenneTwisterFast random)
    {
        //there ought to be a lot of maps in here, one for each species
        Preconditions.checkArgument(!weightMaps.isEmpty());

        double[] sums = new double[allocators.size()];

        //for each tile
        for(SeaTile tile : map.getAllSeaTilesAsList())
        {
            //for each species
            for (Map.Entry<Species, Map<SeaTile, Double>> speciesMap : weightMaps.entrySet())
            {
                //what's the weight here?
                int speciesIndex = speciesMap.getKey().getIndex();
                double weightHere =
                        //weight is always 0 above ground
                        tile.getAltitude() >= 0 ?
                                0:
                                //otherwise allocate according to the right allocator
                                allocators.get(speciesIndex).allocate(
                                        tile,
                                        map,
                                        random
                                );
                sums[speciesIndex]+=weightHere;
                //add to map
                speciesMap.getValue().put(tile,weightHere);

            }
        }

        //normalize if needed
        if(normalize)
            normalizeMaps(sums,map);

    }


    /**
     * returns the weight associated with this tile and this species
     * @param species t
     * @param tile
     * @return
     */
    public double getWeight(Species species, SeaTile tile ){

        return weightMaps.get(species).get(tile);
    }

    private void normalizeMaps(double[] sums,NauticalMap map)
    {
        List<SeaTile> tiles = map.getAllSeaTilesAsList();

        //go through every item and divide it
        for (Map.Entry<Species, Map<SeaTile, Double>> speciesMap : weightMaps.entrySet())
        {
            //what's the weight here?
            int speciesIndex = speciesMap.getKey().getIndex();

            for(SeaTile tile : tiles)
            {

                //normalize
                Map<SeaTile, Double> currentMapping = speciesMap.getValue();
                currentMapping.put(tile, currentMapping.get(tile)/sums[speciesIndex]);

            }


        }
    }

}
