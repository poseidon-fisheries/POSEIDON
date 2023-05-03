package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedList;
import java.util.List;

public class MultipleIndependentSpeciesAbundanceFactory implements
        AlgorithmFactory<MultipleIndependentSpeciesAbundanceInitializer> {





    private List<AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer>> factories = new LinkedList<>();

    //add other species!
    private boolean addGarbageSpecies = true;

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

        for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : factories) {
            initializers.add(factory.apply(fishState));

        }

        addGarbageSpecies = true;
        return new MultipleIndependentSpeciesAbundanceInitializer(initializers, addGarbageSpecies);


    }


    /**
     * Getter for property 'addGarbageSpecies'.
     *
     * @return Value for property 'addGarbageSpecies'.
     */
    public boolean isAddGarbageSpecies() {
        return addGarbageSpecies;
    }

    /**
     * Setter for property 'addGarbageSpecies'.
     *
     * @param addGarbageSpecies Value to set for property 'addGarbageSpecies'.
     */
    public void setAddGarbageSpecies(boolean addGarbageSpecies) {
        this.addGarbageSpecies = addGarbageSpecies;
    }

    /**
     * Getter for property 'factories'.
     *
     * @return Value for property 'factories'.
     */
    public List<AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer>> getFactories() {
        return factories;
    }

    /**
     * Setter for property 'factories'.
     *
     * @param factories Value to set for property 'factories'.
     */
    public void setFactories(
            List<AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer>> factories) {
        this.factories = factories;
    }
}
