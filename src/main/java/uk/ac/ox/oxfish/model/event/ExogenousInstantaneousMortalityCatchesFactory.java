package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class ExogenousInstantaneousMortalityCatchesFactory implements
        AlgorithmFactory<ExogenousInstantaneousMortalityCatches> {


    private LinkedHashMap<String, Double> exogenousMortalities = new LinkedHashMap();

    private boolean isAbundanceBased = true;


    @Override
    public ExogenousInstantaneousMortalityCatches apply(FishState fishState) {



        //useless in almost all cases, but sometimes YAML forces a string in there!
        final LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : exogenousMortalities.entrySet()) {
            copy.put(entry.getKey(), Double.parseDouble(((Object) entry.getValue()).toString()));

        }


        return new ExogenousInstantaneousMortalityCatches(
                "Exogenous landings of ",
                copy,
                isAbundanceBased
        );

    }





    public LinkedHashMap<String, Double> getExogenousMortalities() {
        return exogenousMortalities;
    }

    public void setExogenousMortalities(LinkedHashMap<String, Double> exogenousMortalities) {
        this.exogenousMortalities = exogenousMortalities;
    }

    public boolean isAbundanceBased() {
        return isAbundanceBased;
    }

    public void setAbundanceBased(boolean abundanceBased) {
        isAbundanceBased = abundanceBased;
    }
}
