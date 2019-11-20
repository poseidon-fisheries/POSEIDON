/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.collectors;

import ec.util.MersenneTwisterFast;
import org.junit.Test;

import static org.junit.Assert.*;

public class CounterSpeedTest {


    @Test
    public void counterSpeed()
    {


        Counter counter = new Counter(IntervalPolicy.EVERY_DAY);


        String[] columns = new String[]{"one","two","three","four"};
        for (String column : columns) {
            counter.addColumn(column);
        }

        MersenneTwisterFast randomizer = new MersenneTwisterFast();
        double valuesToAdd[] = new double[100000];
        int colsToPick[] = new int[100000];
        for(int i=0; i<100000; i++)
        {
            valuesToAdd[i] = randomizer.nextDouble()*1000;
            colsToPick[i] = randomizer.nextInt(4);
        }


        long start = System.currentTimeMillis();
        for(int i=0; i<100000; i++)
        {
            counter.count(columns[colsToPick[i]],valuesToAdd[i]);
        }
        long end = System.currentTimeMillis();

        System.out.println((end-start)/1000d);



    }
}