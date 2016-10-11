package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.io.Serializable;

/**
 * Retrieves the variable of type T associated with a particular F (usually a fisher)
 * Created by carrknight on 8/6/15.
 */
public interface Sensor<F,T> extends Serializable{

    T scan(F fisher);
}
