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


        return new ExogenousInstantaneousMortalityCatches(
                "Exogenous catches of ",
                new LinkedHashMap<>(exogenousMortalities),
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
