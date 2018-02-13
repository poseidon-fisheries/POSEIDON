package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceBasedLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultipleIndependentSpeciesAbundanceInitializer implements BiologyInitializer {


    private final List<SingleSpeciesAbundanceInitializer> individualInitializers;


    public MultipleIndependentSpeciesAbundanceInitializer(List<SingleSpeciesAbundanceInitializer> individualInitializers)
    {
        this.individualInitializers = individualInitializers;
    }

    @Override
    public LocalBiology generateLocal(GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells, int mapWidthInCells, NauticalMap map) {
        //generate them locally
        //you call each because each abundance initializer has to weigh each seatile
        //depending on its abundance algorithm
        LocalBiology toReturn = null;
        for(SingleSpeciesAbundanceInitializer initializer : individualInitializers) {
            LocalBiology lastgen = initializer.generateLocal(biology, seaTile, random, mapHeightInCells, mapWidthInCells, map);
            if(toReturn == null ||
                    (toReturn instanceof EmptyLocalBiology && lastgen instanceof AbundanceBasedLocalBiology))
                toReturn = lastgen;
        }
        //return one, it doesn't matter which
        assert toReturn != null;
        return toReturn;
    }

    @Override
    public void processMap(GlobalBiology biology,
                           NauticalMap map,
                           MersenneTwisterFast random,
                           FishState model) {
        for(SingleSpeciesAbundanceInitializer initializer : individualInitializers)
            initializer.processMap(biology,map,random,model);
    }

    /**
     * call global generation for each, grab the species built and move on
     * @param random the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {


        List<Species> species = new ArrayList<>();
        for(SingleSpeciesAbundanceInitializer initializer : individualInitializers)
            species.add(initializer.generateGlobal(random, modelBeingInitialized).getSpecie(0));

        return new GlobalBiology(species.toArray(new Species[species.size()]));

    }
}
