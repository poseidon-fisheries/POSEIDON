package uk.ac.ox.oxfish.biology;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;

/**
 * calls biomass allocator at the end of each year to reset the location of biomass left.
 * If the habitat doesn't support it, the biomass will die in the transportation
 */
public class BiomassLocationResetter implements AdditionalStartable, Steppable
{

    private final Species species;

    private final BiomassAllocator biomassAllocator;


    public BiomassLocationResetter(Species species, BiomassAllocator biomassAllocator) {
        this.species = species;
        this.biomassAllocator = biomassAllocator;
    }


    public void start(FishState model) {
        model.scheduleEveryYear(this,StepOrder.BIOLOGY_PHASE);
    }


    public void turnOff() {

    }

    @Override
    public void step(SimState simState) {

        FishState state = (FishState) simState;

        Double totalAllocation = 0d;
        double totalBiomass = 0d;
        HashMap<SeaTile,Double> hashMap = new HashMap<>();
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList()) {
            //skip if it's unlivable
            if(tile.getBiology() instanceof BiomassLocalBiology)
            {
                if(((BiomassLocalBiology) tile.getBiology()).getCarryingCapacity(species)<=0)
                    continue;
                else
                    totalBiomass+=
                            tile.getBiology().getBiomass(species);
            }
            else
                assert tile.getBiology() instanceof EmptyLocalBiology;

            double allocated = biomassAllocator.allocate(
                    tile,
                    state.getMap(),
                    state.getRandom()
            );
            hashMap.put(tile,
                    allocated);
            totalAllocation+=allocated;


        }
        //now loop again and place it!
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
        {
            if(tile.getBiology() instanceof BiomassLocalBiology)
            {
                if (((BiomassLocalBiology) tile.getBiology()).getCarryingCapacity(species) <= 0)
                {
                    ((BiomassLocalBiology) tile.getBiology()).setCurrentBiomass(species,
                            totalBiomass*hashMap.get(tile)/totalAllocation
                            );
                }

            }



        }
    }
}
