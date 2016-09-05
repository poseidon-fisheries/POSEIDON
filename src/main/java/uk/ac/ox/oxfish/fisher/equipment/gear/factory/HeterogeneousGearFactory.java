package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by carrknight on 5/17/16.
 */
public class HeterogeneousGearFactory implements AlgorithmFactory<HeterogeneousAbundanceGear> {


    public HashMap<String, HomogeneousGearFactory> gears = new HashMap<>();


    public HeterogeneousGearFactory() {
        System.out.println("done");
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
        if(!(gears.values().iterator().next() instanceof  HomogeneousGearFactory))
        {
            //there is an annoying bug with yaml that doesn't really read maps correctly
            //so we'll have to force it here
            FishYAML yaml = new FishYAML();
            HashMap<String, HomogeneousGearFactory> cleaned = new LinkedHashMap<>();
            for(Map.Entry entry : gears.entrySet())
            {
                String key = (String) entry.getKey();
                HashMap<String,LinkedHashMap<String,String>> container = (HashMap<String, LinkedHashMap<String,String>>) entry.getValue();
                assert container.size()==1;
                Map.Entry<String,LinkedHashMap<String,String>> constructor = container.entrySet().iterator().next();
                StringBuilder cleanedYaml = new StringBuilder();
                cleanedYaml.append(constructor.getKey()).append(":").append("\n");
                for(Map.Entry parameter : constructor.getValue().entrySet())
                {
                    if(parameter.getValue()!=null)
                        cleanedYaml.append("  ").append(parameter.getKey().toString()).append(": '").append(parameter.getValue().toString()).append("'").append("\n");
                }
                cleaned.put(key,yaml.loadAs(cleanedYaml.toString(),HomogeneousGearFactory.class));
            }

            gears = cleaned;
        }
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
