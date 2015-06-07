package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Strategy/Scenario factories sometimes get called multiple times and often we'd like each time they are called to return
 * a slightly different value: for example we'd like each FishUntilFull strategy to have a different minimumPercentageFull
 * each time is created.
 * To do that we use DoubleParameter (or one of its cognate) which is just a supplier of double values
 * Created by carrknight on 6/7/15.
 */
public interface DoubleParameter extends Function<MersenneTwisterFast,Double>
{

}
