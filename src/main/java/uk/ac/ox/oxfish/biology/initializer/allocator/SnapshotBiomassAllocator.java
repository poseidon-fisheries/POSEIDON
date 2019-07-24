package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;
import java.util.function.BiFunction;

/**
 * takes a snapshot of the current distribution of biomass and stores it as an array to allocate biomass when asked again
 */
public class SnapshotBiomassAllocator implements BiomassAllocator {

    private HashMap<SeaTile, Double> weightMap = null;


    public void takeSnapshort(NauticalMap map,
                              Species species){
        weightMap = new HashMap<>();
        double total = 0;
        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            if(!seaTile.isFishingEvenPossibleHere())
                continue;
            Double biomassHere = seaTile.getBiomass(species);
            Preconditions.checkState(Double.isFinite(biomassHere));
            total+=biomassHere;
            weightMap.put(seaTile, biomassHere);
        }
        //now again, normalize to 1
        double finalTotal = total;
        Preconditions.checkState(total>0, "nothing to reallocate for " + species);
        weightMap.replaceAll((seaTile, oldbiomass) -> oldbiomass/ finalTotal);


        assert checkItIsNormalized();


    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {

        Preconditions.checkArgument(weightMap!=null, "no snapshot taken yet!");
        return weightMap.getOrDefault(tile,0d);
    }

    private boolean checkItIsNormalized(){
        double total = 0;
        for (Double value : weightMap.values()) {
            total+=value;
        }
        return Math.abs(total-1)<=.0001;
    }
}
