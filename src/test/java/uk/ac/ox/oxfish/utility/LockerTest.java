package uk.ac.ox.oxfish.utility;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/15/16.
 */
public class LockerTest {


    @Test
    public void locker() throws Exception {


        Locker<String,String> locker = new Locker<>();
        String item = locker.presentKey("key", () -> "old_item");
        assertEquals(item,"old_item");
        item = locker.presentKey("key", () -> "other_item");
        assertEquals(item,"old_item");
        item = locker.presentKey("new_key", () -> "new_item");
        assertEquals(item,"new_item");


    }
}