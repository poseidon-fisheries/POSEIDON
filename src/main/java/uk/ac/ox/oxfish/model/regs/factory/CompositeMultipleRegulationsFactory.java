package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.ArrayList;
import java.util.List;

public class CompositeMultipleRegulationsFactory implements AlgorithmFactory<MultipleRegulations> {

    private List<? extends AlgorithmFactory<MultipleRegulations>> multipleRegulationFactories;

    @SuppressWarnings("unused")
    public CompositeMultipleRegulationsFactory() {
        multipleRegulationFactories = new ArrayList<>();
    }

    public CompositeMultipleRegulationsFactory(
        List<? extends AlgorithmFactory<MultipleRegulations>> multipleRegulationFactories
    ) {
        this.multipleRegulationFactories = multipleRegulationFactories;
    }

    @Override
    public MultipleRegulations apply(FishState fishState) {

        final Multimap<String, AlgorithmFactory<? extends Regulation>> factoriesByTag =
            MultimapBuilder.hashKeys().linkedListValues().build();

        getMultipleRegulationFactories().stream()
            .map(factory -> factory.apply(fishState).getFactoriesByTag())
            .forEach(map -> map.forEach(factoriesByTag::putAll));

        return new MultipleRegulations(factoriesByTag);
    }

    public List<? extends AlgorithmFactory<MultipleRegulations>> getMultipleRegulationFactories() {
        return multipleRegulationFactories;
    }

    @SuppressWarnings("unused")
    public void setMultipleRegulationFactories(
        List<? extends AlgorithmFactory<MultipleRegulations>> multipleRegulationFactories
    ) {
        this.multipleRegulationFactories = multipleRegulationFactories;
    }
}
