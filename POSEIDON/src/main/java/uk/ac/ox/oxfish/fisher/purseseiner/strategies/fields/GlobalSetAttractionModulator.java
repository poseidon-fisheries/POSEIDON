package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.function.DoubleUnaryOperator;

public class GlobalSetAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctHoldAvailableModulationFunction;
    private final DoubleUnaryOperator pctSetsRemainingModulationFunction;

    public GlobalSetAttractionModulator(
        final DoubleUnaryOperator pctHoldAvailableModulationFunction,
        final DoubleUnaryOperator pctSetsRemainingModulationFunction
    ) {
        this.pctHoldAvailableModulationFunction = pctHoldAvailableModulationFunction;
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        final double modulatedPctHoldAvailable =
            pctHoldAvailableModulationFunction.applyAsDouble(
                1 - fisher.getHold().getPercentageFilled());
        final double modulatedPctSetsRemaining =
            pctSetsRemainingModulationFunction.applyAsDouble(pctSetsRemaining(fisher));
        return modulatedPctHoldAvailable * modulatedPctSetsRemaining;
    }

    private static double pctSetsRemaining(final Fisher fisher) {
        throw new RuntimeException("Need to be reimplemented with new regulations.");
//        final FadManager fadManager = getFadManager(fisher);
//        final PurseSeinerActionContext actionContext = fadManager.getActionContext();
//        final ImmutableSet<YearlyActionCountLimit> yearlyActionCountLimits =
//            fadManager
//                .getRegulation()
//                .asStream()
//                .filter(reg -> reg instanceof YearlyActionCountLimit)
//                .map(reg -> (YearlyActionCountLimit) reg)
//                .collect(toImmutableSet());
//
//        // Take the highest percentage for every applicable limit for
//        // every action code and return the maximum of all of those.
//        return SET_ACTION_CODES
//            .stream()
//            .flatMapToDouble(actionCode -> {
//                final int count =
//                    actionContext.getCount(fisher.grabState().getCalendarYear(), fisher, actionCode);
//                return yearlyActionCountLimits
//                    .stream()
//                    .flatMapToDouble(reg ->
//                        reg.getApplicableLimits(actionCode).mapToDouble(Map.Entry::getValue)
//                    )
//                    .map(limit -> count / limit);
//            })
//            .max()
//            .orElse(1.0);
    }

}
