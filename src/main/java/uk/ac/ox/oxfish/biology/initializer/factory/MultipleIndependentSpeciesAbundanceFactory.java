package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public class MultipleIndependentSpeciesAbundanceFactory implements
        AlgorithmFactory<MultipleIndependentSpeciesAbundanceInitializer> {



    private List<SingleSpeciesAbundanceFactory> factories = new LinkedList<>();
    {
        SingleSpeciesAbundanceFactory first = new SingleSpeciesAbundanceFactory();
        first.setSpeciesName("Red Fish");
        factories.add(first);
        SingleSpeciesAbundanceFactory second = new SingleSpeciesAbundanceFactory();
        second.setSpeciesName("Blue Fish");
        factories.add(second);

    }

    @Override
    public MultipleIndependentSpeciesAbundanceInitializer apply(FishState fishState) {

        LinkedList<SingleSpeciesAbundanceInitializer> initializers =
                new LinkedList<>();

        for(SingleSpeciesAbundanceFactory factory : factories)
            initializers.add(factory.apply(fishState));

        return new MultipleIndependentSpeciesAbundanceInitializer(initializers);


    }


    public List<SingleSpeciesAbundanceFactory> getFactories() {
        return factories;
    }

    public void setFactories(List<SingleSpeciesAbundanceFactory> factories) {
        this.factories = factories;
    }
}
