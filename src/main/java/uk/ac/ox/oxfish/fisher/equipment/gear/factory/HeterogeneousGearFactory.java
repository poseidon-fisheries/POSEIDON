package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousGearFactory implements AlgorithmFactory<HeterogeneousAbundanceGear> {


    public HashMap<String, HomogeneousGearFactory> gears = new HashMap<>();


    public HeterogeneousGearFactory() {
    }

    public HeterogeneousGearFactory(Pair<String,HomogeneousGearFactory>...
                                    given) {
        for(Pair<String,HomogeneousGearFactory> pair : given)
        {
            gears.put(pair.getFirst(),pair.getSecond());
        }
    }


    /**
     * Getter for property 'gears'.
     *
     * @return Value for property 'gears'.
     */
    public HashMap<String, HomogeneousGearFactory> getGears() {
        return gears;
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HeterogeneousAbundanceGear apply(FishState state) {

        HashMap<Species, HomogeneousAbundanceGear> gearsPerSpecies = new HashMap<>();

        for(Map.Entry<String,HomogeneousGearFactory>
                entry : gears.entrySet())
        {
            gearsPerSpecies.put(
                    state.getBiology().getSpecie(entry.getKey()),
                    entry.getValue().apply(state)
            );
        }
        Preconditions.checkState(gears.size()==state.getSpecies().size(), "Not all species have a gear assigned");
        return new HeterogeneousAbundanceGear(gearsPerSpecies);

    }

    /**
     * Setter for property 'gears'.
     *
     * @param gears Value to set for property 'gears'.
     */
    public void setGears(
            HashMap<String, HomogeneousGearFactory> gears) {
        this.gears = gears;
    }





}
