package uk.ac.ox.oxfish.model.data;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A serializable function
 * Created by carrknight on 4/20/16.
 */
public interface Gatherer<T> extends Function<T,Double>, Serializable {
}
