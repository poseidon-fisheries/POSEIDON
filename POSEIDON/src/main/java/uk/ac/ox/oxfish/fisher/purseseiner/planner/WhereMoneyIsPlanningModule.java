package uk.ac.ox.oxfish.fisher.purseseiner.planner;

/**
 *  (best area has max SUM(FAD_VALUE^VALUE_WEIGHT)
 */
public class WhereMoneyIsPlanningModule extends PickBestPilePlanningModule {

    private final double valueWeight;


    public WhereMoneyIsPlanningModule(OwnFadSetDiscretizedActionGenerator optionsGenerator,
                                      double valueWeight) {
        super(optionsGenerator);
        this.valueWeight = valueWeight;
    }

    @Override
    protected double weighFad(int currentModelStep, OwnFadSetDiscretizedActionGenerator.ValuedFad valuedFad) {
        return Math.pow(valuedFad.getSecond(),valueWeight);
    }


    public double getValueWeight() {
        return valueWeight;
    }
}
