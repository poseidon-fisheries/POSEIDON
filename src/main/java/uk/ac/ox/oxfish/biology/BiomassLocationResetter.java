package uk.ac.ox.oxfish.biology;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * calls biomass allocator at the end of each year to reset the location of biomass left.
 * If the habitat doesn't support it, the biomass will die in the transportation
 */
public class BiomassLocationResetter implements AdditionalStartable, Steppable
{

    private final Species species;

    //a supplier because I want a "new" biomass allocator each time step
    private final Supplier<BiomassAllocator> biomassAllocator;


    public BiomassLocationResetter(Species species, Supplier<BiomassAllocator> biomassAllocator) {
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

        BiomassAllocator thisYearAllocator = this.biomassAllocator.get();
        Double totalAllocation = 0d;
        double totalBiomass = computeBiomassNextYear((FishState) simState);
        HashMap<SeaTile,Double> hashMap = new HashMap<>();
        //for all the areas of the seas that are livable
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList()) {
            //skip if it's unlivable

            if(((VariableBiomassBasedBiology) tile.getBiology()).getCarryingCapacity(species)<=0)
                continue;


            //allocate new biomass weight
            double allocated = thisYearAllocator.allocate(
                    tile,
                    state.getMap(),
                    state.getRandom()
            );

            if(!Double.isFinite(allocated))
                allocated=0;
            hashMap.put(tile,
                        allocated);
            totalAllocation += allocated;


        }
        assert Double.isFinite(totalAllocation);
        assert  totalAllocation>=0;

        //now loop again and place it!
        for(SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList())
        {
            if(tile.getBiology() instanceof VariableBiomassBasedBiology)
            {
                VariableBiomassBasedBiology biology = (VariableBiomassBasedBiology) tile.getBiology();
                if (biology.getCarryingCapacity(species) > 0)
                {
                    biology.setCurrentBiomass(species,
                                              Math.min(
                                                      totalBiomass*hashMap.get(tile)/totalAllocation,
                                                      biology.getCarryingCapacity(species))
                    );
                }

            }



        }
    }

    protected double computeBiomassNextYear(FishState simState) {
        return simState.getTotalBiomass(species);
    }
}
