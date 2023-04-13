package uk.ac.ox.oxfish.fisher.purseseiner.planner;

/**
 * (best area has max SUM(FAD_VALUE^VALUE_WEIGHT)
 */
public class WhereMoneyIsPlanningModule extends PickBestPilePlanningModule {

    private final double valueWeight;


    public WhereMoneyIsPlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator,
        final double valueWeight
    ) {
        super(optionsGenerator);
        this.valueWeight = valueWeight;
    }

    @Override
    protected double weighFad(final int currentModelStep, final OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad) {
        return Math.pow(valuedFad.getValue(), valueWeight);
    }


    public double getValueWeight() {
        return valueWeight;
    }
}
