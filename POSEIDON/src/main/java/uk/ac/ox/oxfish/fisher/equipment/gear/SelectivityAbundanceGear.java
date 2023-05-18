package uk.ac.ox.oxfish.fisher.equipment.gear;

import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;

/**
 * homogeneous gear that keeps track of its own retention and selectivity curve; this makes them easier to monitor
 */
public class SelectivityAbundanceGear extends HomogeneousAbundanceGear {


    private final FixedProportionFilter catchability;

    private final LogisticAbundanceFilter selectivity;

    private final RetentionAbundanceFilter retention;


    public SelectivityAbundanceGear(
        final double litersOfGasConsumedEachHourFishing,
        final FixedProportionFilter catchability,
        final LogisticAbundanceFilter selectivity
    ) {
        super(litersOfGasConsumedEachHourFishing, catchability, selectivity);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = null;
    }

    public SelectivityAbundanceGear(
        final double litersOfGasConsumedEachHourFishing,
        final FixedProportionFilter catchability,
        final LogisticAbundanceFilter selectivity,
        final RetentionAbundanceFilter retention
    ) {
        super(litersOfGasConsumedEachHourFishing, catchability, selectivity, retention);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = retention;
    }

    @Override
    public Gear makeCopy() {
        if (retention == null)
            return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                catchability, selectivity
            );
        else
            return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                catchability, selectivity, retention
            );
    }


    public double getaParameter() {
        return selectivity.getaParameter();
    }

    public double getbParameter() {
        return selectivity.getbParameter();
    }

    public double getCatchability() {
        return catchability.getProportion();
    }

    public FixedProportionFilter getCatchabilityFilter() {
        return catchability;
    }

    public LogisticAbundanceFilter getSelectivity() {
        return selectivity;
    }

    public RetentionAbundanceFilter getRetention() {
        return retention;
    }
}
