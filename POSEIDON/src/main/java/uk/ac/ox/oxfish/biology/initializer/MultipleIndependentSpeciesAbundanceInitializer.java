package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer.FAKE_SPECIES_NAME;

public class MultipleIndependentSpeciesAbundanceInitializer implements BiologyInitializer {


    private final List<SingleSpeciesAbundanceInitializer> individualInitializers;


    private boolean addOtherSpecies;


    public MultipleIndependentSpeciesAbundanceInitializer(
            List<SingleSpeciesAbundanceInitializer> individualInitializers, boolean addOtherSpecies)
    {
        this.individualInitializers = individualInitializers;
        this.addOtherSpecies = addOtherSpecies;
    }

    @Override
    public LocalBiology generateLocal(GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells, int mapWidthInCells, NauticalMap map) {
        //generate them locally
        //you call each because each abundance initializer has to weigh each seatile
        //depending on its abundance algorithm
        LocalBiology toReturn = null;
        for(SingleSpeciesAbundanceInitializer initializer : individualInitializers) {
            LocalBiology lastgen = initializer.generateLocal(biology,
                                                             seaTile,
                                                             random,
                                                             mapHeightInCells,
                                                             mapWidthInCells,
                                                             map);
            if(toReturn == null ||
                    (toReturn instanceof EmptyLocalBiology && lastgen instanceof AbundanceLocalBiology))
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

        //need to add an additional species to catch "all"
        if(addOtherSpecies)
            species.add(new Species(FAKE_SPECIES_NAME,
                                    StockAssessmentCaliforniaMeristics.FAKE_MERISTICS,
                                    true));

        return new GlobalBiology(species.toArray(new Species[species.size()]));

    }
}
