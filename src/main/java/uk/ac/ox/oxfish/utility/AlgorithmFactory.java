package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * It will be useful to have strategies created by a factory so that I can have the JSON and GUI instantiation separated
 * from the strategy itself. This is the main interface holding everything together
 *
 * Created by carrknight on 5/27/15.
 */
public interface AlgorithmFactory<T> extends Function<FishState,T>
{


}



