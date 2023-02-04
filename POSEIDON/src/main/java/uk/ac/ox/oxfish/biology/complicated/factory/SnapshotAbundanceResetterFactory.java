package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.SnapshotBiologyResetter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class SnapshotAbundanceResetterFactory implements AlgorithmFactory<SnapshotBiologyResetter> {


    private int yearsToReset = 1;

    private boolean restoreOriginalLocations = false;

    private boolean restoreOriginalLengthDistribution = true;

    @Override
    public SnapshotBiologyResetter apply(FishState state) {
        return SnapshotBiologyResetter.abundanceResetter(state.getBiology(), yearsToReset,restoreOriginalLocations,
                                                         restoreOriginalLengthDistribution );
    }


    public int getYearsToReset() {
        return yearsToReset;
    }

    public void setYearsToReset(int yearsToReset) {
        this.yearsToReset = yearsToReset;
    }

    /**
     * Getter for property 'restoreOriginalLocations'.
     *
     * @return Value for property 'restoreOriginalLocations'.
     */
    public boolean isRestoreOriginalLocations() {
        return restoreOriginalLocations;
    }

    /**
     * Setter for property 'restoreOriginalLocations'.
     *
     * @param restoreOriginalLocations Value to set for property 'restoreOriginalLocations'.
     */
    public void setRestoreOriginalLocations(boolean restoreOriginalLocations) {
        this.restoreOriginalLocations = restoreOriginalLocations;
    }

    /**
     * Getter for property 'restoreOriginalLengthDistribution'.
     *
     * @return Value for property 'restoreOriginalLengthDistribution'.
     */
    public boolean isRestoreOriginalLengthDistribution() {
        return restoreOriginalLengthDistribution;
    }

    /**
     * Setter for property 'restoreOriginalLengthDistribution'.
     *
     * @param restoreOriginalLengthDistribution Value to set for property 'restoreOriginalLengthDistribution'.
     */
    public void setRestoreOriginalLengthDistribution(boolean restoreOriginalLengthDistribution) {
        this.restoreOriginalLengthDistribution = restoreOriginalLengthDistribution;
    }
}
