package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;

public class EpoScenarioTest extends TestCase {

    public <S extends EpoScenario<?, ?>> void saveAndLoadYaml(
        final Class<S> scenarioClass
    ) {
        try {
            final S constructedScenario = scenarioClass.newInstance();
            final File scenarioFile = constructedScenario.testFolder
                .resolve(scenarioClass.getSimpleName() + ".yaml")
                .toFile();
            new FishYAML().dump(constructedScenario, new FileWriter(scenarioFile));
            // Try to read it back and start it
            try (final FileReader fileReader = new FileReader(scenarioFile)) {
                final FishYAML fishYAML = new FishYAML();
                final S loadedScenario = fishYAML.loadAs(fileReader, scenarioClass);
                loadedScenario.useDummyData();
                final FishState fishState = new FishState();
                fishState.setScenario(loadedScenario);
                fishState.start();
            } catch (final FileNotFoundException e) {
                throw new IllegalArgumentException("Can't find scenario file: " + scenarioFile, e);
            } catch (final IOException e) {
                throw new IllegalStateException("Error while reading file: " + scenarioFile, e);
            }
        } catch (final IOException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

    }

    public void testSaveAndLoadScenarios() {
        ImmutableList.of(
            EpoAbundanceScenario.class,
            EpoBiomassScenario.class,
            FadsOnlyEpoAbundanceScenario.class,
            EpoAbundanceScenarioBioOnly.class,
            EpoScenarioPathfinding.class
        ).forEach(this::saveAndLoadYaml);
    }
}