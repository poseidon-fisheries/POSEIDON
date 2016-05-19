package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * An interface that makes sure all the implementers can have their catchability modified
 * Created by carrknight on 5/18/16.
 */
public interface HomogeneousGearFactory extends AlgorithmFactory<HomogeneousAbundanceGear>
{


    /**
     * Getter for property 'averageCatchability'.
     *
     * @return Value for property 'averageCatchability'.
     */
    public DoubleParameter getAverageCatchability();

    /**
     * Setter for property 'averageCatchability'.
     *
     * @param averageCatchability Value to set for property 'averageCatchability'.
     */
    public void setAverageCatchability(DoubleParameter averageCatchability);

}
