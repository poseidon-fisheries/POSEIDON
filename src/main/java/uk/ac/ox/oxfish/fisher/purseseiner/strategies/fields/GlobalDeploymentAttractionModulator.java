package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.CompressedExponentialFunction;

import java.util.function.DoubleUnaryOperator;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class GlobalDeploymentAttractionModulator implements GlobalAttractionModulator {

    private final DoubleUnaryOperator pctActiveFadsLimitModulationFunction;
    private final DoubleUnaryOperator numFadsInStockModulationFunction;

    public GlobalDeploymentAttractionModulator(
        final double pctActiveFadsLimitCoefficient,
        final double pctActiveFadsLimitExponent,
        final double numFadsInStockCoefficient,
        final double numFadsInStockExponent
    ) {
        this(
            new CompressedExponentialFunction(pctActiveFadsLimitCoefficient, pctActiveFadsLimitExponent),
            new CompressedExponentialFunction(numFadsInStockCoefficient, numFadsInStockExponent)
        );
    }

    private GlobalDeploymentAttractionModulator(
        final DoubleUnaryOperator pctActiveFadsLimitModulationFunction,
        final DoubleUnaryOperator numFadsInStockModulationFunction
    ) {
        this.pctActiveFadsLimitModulationFunction = pctActiveFadsLimitModulationFunction;
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    @Override
    public double modulate(final Fisher fisher) {
        return pctActiveFadsLimitModulationFunction.applyAsDouble(1 - getPctActiveFads(fisher))
            * numFadsInStockModulationFunction.applyAsDouble(getFadManager(fisher).getNumFadsInStock());
    }

    private static double getPctActiveFads(final Fisher fisher) {
        final FadManager<? extends LocalBiology, ? extends Fad<?, ?>> fadManager =
            FadManager.getFadManager(fisher);
        return fadManager
            .getActionSpecificRegulations()
            .getActiveFadLimits()
            .map(reg -> (double) fadManager.getNumDeployedFads() / reg.getLimit(fisher))
            .orElse(0.0);
    }
}
