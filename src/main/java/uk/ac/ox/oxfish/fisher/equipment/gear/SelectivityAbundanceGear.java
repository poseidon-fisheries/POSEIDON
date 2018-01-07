package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;

/**
 * homogeneous gear that keeps track of its own retention and selectivity curve; this makes them easier to monitor
 */
public class SelectivityAbundanceGear extends HomogeneousAbundanceGear {


    @NotNull
    private final FixedProportionFilter catchability;

    @NotNull
    private final LogisticAbundanceFilter selectivity;

    @Nullable
    private final RetentionAbundanceFilter retention;


    public SelectivityAbundanceGear(double litersOfGasConsumedEachHourFishing,
                                    @NotNull FixedProportionFilter catchability,
                                    @NotNull LogisticAbundanceFilter selectivity) {
        super(litersOfGasConsumedEachHourFishing, catchability,selectivity);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = null;
    }

    public SelectivityAbundanceGear(double litersOfGasConsumedEachHourFishing,
                                    @NotNull FixedProportionFilter catchability,
                                    @NotNull LogisticAbundanceFilter selectivity,
                                    @NotNull RetentionAbundanceFilter retention) {
        super(litersOfGasConsumedEachHourFishing, catchability,selectivity,retention);
        this.catchability = catchability;
        this.selectivity = selectivity;
        this.retention = retention;
    }

    @Override
    public Gear makeCopy() {
        if(retention == null)
                return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                        catchability,selectivity);
        else
            return new SelectivityAbundanceGear(getLitersOfGasConsumedEachHourFishing(),
                    catchability,selectivity,retention);
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
    @NotNull
    public LogisticAbundanceFilter getSelectivity() {
        return selectivity;
    }

    @Nullable
    public RetentionAbundanceFilter getRetention() {
        return retention;
    }
}
