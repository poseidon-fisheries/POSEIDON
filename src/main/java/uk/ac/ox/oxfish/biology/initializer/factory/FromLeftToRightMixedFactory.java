package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.FromLeftToRightMixedInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;


public class FromLeftToRightMixedFactory implements AlgorithmFactory<FromLeftToRightMixedInitializer>{


    /**
     * second biomass = first biomass * this value
     */
    private DoubleParameter proportionSecondSpeciesToFirst = new FixedDoubleParameter(1);

    /**
     * leftmost biomass
     */
    private DoubleParameter maximumBiomass = new FixedDoubleParameter(5000);

    /**
     * the first species' name
     */
    private String firstSpeciesName = "Species 0";

    /**
     * the second species' name
     */
    private String secondSpeciesName = "Species 1";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FromLeftToRightMixedInitializer apply(FishState state) {
        FromLeftToRightMixedInitializer initializer = new FromLeftToRightMixedInitializer(
                maximumBiomass.apply(state.getRandom()),
                proportionSecondSpeciesToFirst.apply(state.getRandom()));
        initializer.setFirstSpeciesName(firstSpeciesName);
        initializer.setSecondSpeciesName(secondSpeciesName);
        return initializer;
    }

    /**
     * Getter for property 'proportionSecondSpeciesToFirst'.
     *
     * @return Value for property 'proportionSecondSpeciesToFirst'.
     */
    public DoubleParameter getProportionSecondSpeciesToFirst() {
        return proportionSecondSpeciesToFirst;
    }

    /**
     * Setter for property 'proportionSecondSpeciesToFirst'.
     *
     * @param proportionSecondSpeciesToFirst Value to set for property 'proportionSecondSpeciesToFirst'.
     */
    public void setProportionSecondSpeciesToFirst(
            DoubleParameter proportionSecondSpeciesToFirst) {
        this.proportionSecondSpeciesToFirst = proportionSecondSpeciesToFirst;
    }

    /**
     * Getter for property 'maximumBiomass'.
     *
     * @return Value for property 'maximumBiomass'.
     */
    public DoubleParameter getMaximumBiomass() {
        return maximumBiomass;
    }

    /**
     * Setter for property 'maximumBiomass'.
     *
     * @param maximumBiomass Value to set for property 'maximumBiomass'.
     */
    public void setMaximumBiomass(DoubleParameter maximumBiomass) {
        this.maximumBiomass = maximumBiomass;
    }

    public String getFirstSpeciesName() {
        return firstSpeciesName;
    }

    public void setFirstSpeciesName(String firstSpeciesName) {
        this.firstSpeciesName = firstSpeciesName;
    }

    public String getSecondSpeciesName() {
        return secondSpeciesName;
    }

    public void setSecondSpeciesName(String secondSpeciesName) {
        this.secondSpeciesName = secondSpeciesName;
    }
}
