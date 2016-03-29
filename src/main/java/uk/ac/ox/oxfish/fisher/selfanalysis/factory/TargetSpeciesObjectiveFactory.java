package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.TargetSpeciesTripObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Builds target species objective
 * Created by carrknight on 3/24/16.
 */
public class TargetSpeciesObjectiveFactory implements AlgorithmFactory<TargetSpeciesTripObjective> {

    private boolean opportunityCosts = true;


    private int speciesIndex = 0;

    public TargetSpeciesObjectiveFactory()
    {


    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TargetSpeciesTripObjective apply(FishState fishState) {
        return new TargetSpeciesTripObjective(fishState.getSpecies().get(speciesIndex),
                                              opportunityCosts);
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Setter for property 'opportunityCosts'.
     *
     * @param opportunityCosts Value to set for property 'opportunityCosts'.
     */
    public void setOpportunityCosts(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }

    /**
     * Getter for property 'speciesIndex'.
     *
     * @return Value for property 'speciesIndex'.
     */
    public int getSpeciesIndex() {
        return speciesIndex;
    }

    /**
     * Setter for property 'speciesIndex'.
     *
     * @param speciesIndex Value to set for property 'speciesIndex'.
     */
    public void setSpeciesIndex(int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }
}
