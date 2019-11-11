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

package uk.ac.ox.oxfish.model;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.dynapro.AmateurishApproximateDynamicProgram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 10/12/16.
 */
public class AmateurishApproximateDynamicProgramTest {


    @Test
    public void shortTermDynamic() throws Exception {

        //it's always better to take action 0 when 2a > b + c (good state)
        //and action 1 viceversa. Can it show in the value function?
        MersenneTwisterFast random = new MersenneTwisterFast();

        AmateurishApproximateDynamicProgram program = new AmateurishApproximateDynamicProgram(2, 3, .05);

        double reward = Double.NaN;
        int actionTaken=-100;
        double oldA = Double.NaN;
        double oldB = Double.NaN;
        double oldC = Double.NaN;
        for(int i=0; i<1000; i++)
        {


            //state variables go from -1 to 1
            double a =  random.nextDouble()*2;
            double b =  random.nextDouble()*2;
            double c =  random.nextDouble()*2;

            //update at the new state if possible
            if(Double.isFinite(oldA))
                program.updateActionDueToImmediateReward(actionTaken,reward,1,new double[]{oldA,oldB,oldC},new double[]{a,b,c});



            //ask program to choose best action
            Pair<Integer, Double> action = program.chooseBestAction(a, b, c);
            actionTaken = action.getFirst();
            boolean goodState = 2 * a > b + c;
            //compute the reward
            if(goodState)
            {
                if (actionTaken == 0) {
                    reward = 1;
                }
                else{
                    reward = -1;
                }
            }
            else{
                if (actionTaken == 0) {
                    reward = -1;
                }
                else{
                    reward = 1;
                }
            }



            oldA = a;
            oldB=b;
            oldC=c;
        }

        System.out.println(program);
        //you should be able to make at least the easy decisions:
        assertEquals(0,(int)program.chooseBestAction(1,.2,.2).getFirst());
        assertEquals(1,(int)program.chooseBestAction(.3,.5,.5).getFirst());

        //and the signs ought to be right
        assertTrue(program.getLinearParameters()[0][0]>0);
        assertTrue(program.getLinearParameters()[1][0]<0);


    }
}