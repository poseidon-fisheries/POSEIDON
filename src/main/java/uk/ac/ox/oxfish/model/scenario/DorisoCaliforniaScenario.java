package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.initializer.AllocatedBiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesDerisoInitializer;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Created by carrknight on 6/19/17.
 */
public class DorisoCaliforniaScenario extends CaliforniaAbstractScenario {



    private MultipleSpeciesDerisoInitializer initializer;


    /**
     * build the biology part!
     *
     * @param model
     * @param folderMap
     * @return
     */
    @Override
    protected GlobalBiology buildBiology(
            FishState model, LinkedHashMap<String, Path> folderMap) {
        initializer = new MultipleSpeciesDerisoInitializer(folderMap,true);

        GlobalBiology biology = initializer.generateGlobal(model.getRandom(),
                                             model);

        return biology;
    }

    @Override
    public AllocatedBiologyInitializer getBiologyInitializer() {
        return initializer;
    }
}
