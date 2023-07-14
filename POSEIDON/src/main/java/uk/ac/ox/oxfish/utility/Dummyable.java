package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.Arrays;

public interface Dummyable {
    static void maybeUseDummyData(final InputPath dummyDataFolder, final Object... potentialDummyables) {
        Arrays.stream(potentialDummyables)
            .filter(Dummyable.class::isInstance)
            .forEach(factory -> ((Dummyable) factory).useDummyData(dummyDataFolder));
    }

    void useDummyData(final InputPath dummyDataFolder);
}
