package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * Retrieves the variable of type T associated with a particular fisher
 * Created by carrknight on 8/6/15.
 */
public interface Sensor<T> {

    T scan(Fisher fisher);
}
