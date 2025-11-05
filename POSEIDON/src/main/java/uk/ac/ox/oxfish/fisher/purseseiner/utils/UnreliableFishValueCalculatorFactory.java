package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.function.DoubleUnaryOperator;

/**
 * Species Blur added for EAO Project of 2025 by Brian Powers
 * The Species blur can blur a certain proportion of each species's biomass under the water
 * This portion of the fish are indistinguishable and are valued at an average price
 *
 * If the blur is at 0 then the echosounder perfectly differentiates tuna species for valuing the fad
 * If the blur is at 1 then the echosounder just looks at total biomass under the FAD and applys an
 * average price.
 * This is calibrated to match behavior of the agents.
 */
public class UnreliableFishValueCalculatorFactory implements AlgorithmFactory<FishValueCalculator> {
    private AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator;
    private DoubleParameter speciesBlur;

    public UnreliableFishValueCalculatorFactory(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator,
        CalibratedParameter speciesBlur
        ) {
        this.errorOperator = errorOperator;
        this.speciesBlur=speciesBlur;
    }

    public UnreliableFishValueCalculatorFactory(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator
    ) {
        this.errorOperator = errorOperator;
        this.speciesBlur= new CalibratedParameter(0);
    }

    public UnreliableFishValueCalculatorFactory() {
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getErrorOperator() {
        return errorOperator;
    }

    public void setErrorOperator(final AlgorithmFactory<? extends DoubleUnaryOperator> errorOperator) {
        this.errorOperator = errorOperator;
    }

    @Override
    public UnreliableFishValueCalculator apply(final FishState fishState) {
        return new UnreliableFishValueCalculator(
            fishState.getBiology(),
            errorOperator.apply(fishState),
            speciesBlur.applyAsDouble(fishState.getRandom())
        );
    }

    public void setSpeciesBlur(DoubleParameter speciesBlur) {
        this.speciesBlur = speciesBlur;
    }

    public DoubleParameter getSpeciesBlur(){
        return speciesBlur;
    }
}
