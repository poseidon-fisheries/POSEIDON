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

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.poseidon.burlap.LogisticMultiClassifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 12/5/16.
 */
public class LogisticMultiClassifierTest {

    @Test
    public void logistic() throws Exception
    {

        double[][] beta = new double[2][2];
        beta[0][0] = 1;
        beta[0][1] = 0;
        beta[1][0] = 0;
        beta[1][1] = 0;



        LogisticMultiClassifier logit = new LogisticMultiClassifier(beta);


        double[][] input = {new double[]{1, 1}, new double[]{1, 1}};

        assertEquals(0.731058, logit.getProbability(0,
                                                    input), .001);
        assertEquals(1d-0.731058, logit.getProbability(
                1, input), .001);


        beta[1][0] = 1;
        assertEquals(0.5,logit.getProbability(
                0,input),.001);
        assertEquals(0.5,logit.getProbability(
                1,input),.001);

        MersenneTwisterFast random = new MersenneTwisterFast();
        int chosen1 = 0;
        for(int i=0; i<1000; i++) {
            if(logit.choose(
                    input, random)==1)
                chosen1++;
        }

        System.out.println(chosen1);
        assertTrue(chosen1>200);
        assertTrue(chosen1<800);

    }
}