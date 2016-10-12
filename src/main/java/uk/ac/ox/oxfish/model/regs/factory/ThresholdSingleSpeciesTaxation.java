package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.policymakers.SingleSpeciesBiomassTaxman;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.WeakHashMap;

/**
 * Created by carrknight on 9/30/16.
 */
public class ThresholdSingleSpeciesTaxation implements AlgorithmFactory<ProtectedAreasOnly> {


    private int speciesIndex = 0;

    private DoubleParameter biomassThreshold =  new FixedDoubleParameter(5000000);

    private DoubleParameter tax = new FixedDoubleParameter(10);

    private boolean taxWhenBelowThreshold = true;


    private final WeakHashMap<FishState,SingleSpeciesBiomassTaxman> taxes = new WeakHashMap<>();

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public ProtectedAreasOnly apply(FishState fishState) {
        ProtectedAreasOnly regulations = new ProtectedAreasOnly();

        if(!taxes.containsKey(fishState)) {
            SingleSpeciesBiomassTaxman taxman = new SingleSpeciesBiomassTaxman(
                    fishState.getSpecies().get(speciesIndex),
                    tax.apply(fishState.getRandom()),
                    biomassThreshold.apply(fishState.getRandom()),
                    taxWhenBelowThreshold
            );

            fishState.registerStartable(taxman);
            taxes.put(fishState,taxman);
        }

        return regulations;

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

    /**
     * Getter for property 'biomassThreshold'.
     *
     * @return Value for property 'biomassThreshold'.
     */
    public DoubleParameter getBiomassThreshold() {
        return biomassThreshold;
    }

    /**
     * Setter for property 'biomassThreshold'.
     *
     * @param biomassThreshold Value to set for property 'biomassThreshold'.
     */
    public void setBiomassThreshold(DoubleParameter biomassThreshold) {
        this.biomassThreshold = biomassThreshold;
    }

    /**
     * Getter for property 'tax'.
     *
     * @return Value for property 'tax'.
     */
    public DoubleParameter getTax() {
        return tax;
    }

    /**
     * Setter for property 'tax'.
     *
     * @param tax Value to set for property 'tax'.
     */
    public void setTax(DoubleParameter tax) {
        this.tax = tax;
    }

    /**
     * Getter for property 'taxWhenBelowThreshold'.
     *
     * @return Value for property 'taxWhenBelowThreshold'.
     */
    public boolean isTaxWhenBelowThreshold() {
        return taxWhenBelowThreshold;
    }

    /**
     * Setter for property 'taxWhenBelowThreshold'.
     *
     * @param taxWhenBelowThreshold Value to set for property 'taxWhenBelowThreshold'.
     */
    public void setTaxWhenBelowThreshold(boolean taxWhenBelowThreshold) {
        this.taxWhenBelowThreshold = taxWhenBelowThreshold;
    }
}
