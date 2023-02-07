package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory;

import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.ExponentialTimeScalar;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The factory to create an exponential time scalar function.
 * @author Brian Powers 5/3/2019
 *
 */

public class ExponentialTimeScalarFactory  implements AlgorithmFactory<ExponentialTimeScalar>{
	private DoubleParameter exponent = new FixedDoubleParameter(1.0);

	@Override
	public ExponentialTimeScalar apply(FishState state) {
		return new ExponentialTimeScalar(exponent.apply(state.random));
	}

	public DoubleParameter getExponent(){
		return exponent;
	}
	
	public void setExponent(DoubleParameter exponent){
		this.exponent = exponent;
	}
}
