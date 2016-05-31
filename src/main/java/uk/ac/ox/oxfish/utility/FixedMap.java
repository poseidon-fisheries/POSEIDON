package uk.ac.ox.oxfish.utility;

import java.util.HashMap;

/**
 * Map that always returns the same value for keys that aren't inserted. Very easy; copied from here:
 * http://stackoverflow.com/questions/28554878/is-it-possible-to-create-a-map-where-every-key-points-to-the-same-value
 * Created by carrknight on 5/31/16.
 */
public class FixedMap<K,V> extends HashMap<K,V> {


    protected V defaultValue;

    public FixedMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }
    @Override
    public V get(Object k) {
        return containsKey(k) ? super.get(k) : defaultValue;
    }

}
