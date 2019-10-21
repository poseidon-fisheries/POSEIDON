package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.SnapshotBiologyResetter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class SnapshotAbundanceResetterFactory implements AlgorithmFactory<SnapshotBiologyResetter> {


    private int yearsToReset = 1;

    @Override
    public SnapshotBiologyResetter apply(FishState state) {
        return SnapshotBiologyResetter.abundanceResetter(state.getBiology(), yearsToReset);
    }


    public int getYearsToReset() {
        return yearsToReset;
    }

    public void setYearsToReset(int yearsToReset) {
        this.yearsToReset = yearsToReset;
    }
}
