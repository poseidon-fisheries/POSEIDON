package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.PurseSeinerActionContext;
import uk.ac.ox.poseidon.regulations.core.YearlyActionCountLimit;

import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction.SET_ACTION_CODES;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

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
        final FadManager fadManager = getFadManager(fisher);
        final PurseSeinerActionContext actionContext = fadManager.getActionContext();
        final ImmutableSet<YearlyActionCountLimit> yearlyActionCountLimits =
            fadManager
                .getRegulations()
                .asStream()
                .filter(reg -> reg instanceof YearlyActionCountLimit)
                .map(reg -> (YearlyActionCountLimit) reg)
                .collect(toImmutableSet());
        
        // Take the highest percentage for every applicable limit for
        // every action code and return the maximum of all of those.
        return SET_ACTION_CODES
            .stream()
            .flatMapToDouble(actionCode -> {
                final int count =
                    actionContext.getCount(fisher.grabState().getCalendarYear(), fisher, actionCode);
                return yearlyActionCountLimits
                    .stream()
                    .flatMapToDouble(reg ->
                        reg.getApplicableLimits(actionCode).mapToDouble(Map.Entry::getValue)
                    )
                    .map(limit -> count / limit);
            })
            .max()
            .orElse(1.0);
    }

}
