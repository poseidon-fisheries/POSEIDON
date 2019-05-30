package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.SnapshotAbundanceResetter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SnapshotAbundanceResetterFactory implements AlgorithmFactory<SnapshotAbundanceResetter> {


    private int yearsToReset = 1;

    @Override
    public SnapshotAbundanceResetter apply(FishState state) {
        return new SnapshotAbundanceResetter(state.getBiology(),yearsToReset);
    }


    public int getYearsToReset() {
        return yearsToReset;
    }

    public void setYearsToReset(int yearsToReset) {
        this.yearsToReset = yearsToReset;
    }
}
