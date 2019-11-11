/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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