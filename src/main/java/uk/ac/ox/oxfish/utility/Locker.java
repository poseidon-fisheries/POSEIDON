package uk.ac.ox.oxfish.utility;


import java.util.function.Supplier;

/**
 * Very simple container. You can put an item in there with a key. If you give back the same key you get back
 * the same item, otherwise the item is destroyed
 * Created by carrknight on 11/15/16.
 */
public class Locker<K,I>
{

    private I itemHeld;

    private K key;

    public Locker() {
    }


    /**
     * if the key is recognized, the old item is given. Otherwise the supplier is used to get a new item out
     * @param key key
     * @param constructor used to create item if the key has changed
     * @return old item if the key is unchanged, new item otherwise
     */
    public I presentKey(K key, Supplier<I> constructor){
        if(key.equals(this.key))
        {
            assert itemHeld != null;
            return itemHeld;
        }
        else
        {
            itemHeld = constructor.get();
            this.key = key;
            return itemHeld;
        }

    }

    /**
     * returns the current Key
     */
    public K getCurrentKey() {
        return key;
    }
}
