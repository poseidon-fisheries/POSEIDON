/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public interface OptimizationParameter {


    /**
     * number of parameters this object actually represents
     * @return
     */
    public int size();

    /**
     * consume the scenario and add the parameters
     * @param scenario the scenario to modify
     * @param inputs the numerical values of the parameters to set
     */
    public double parametrize(Scenario scenario, double inputs[] );





    public static void navigateAndSet(Scenario scenario,
                                      String address,
                                      Object value) throws IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        String[] steps = address.split("\\.");
        Object node = scenario;
        for(int i=0; i<steps.length-1; i++) {
            String step = steps[i];
            if(step.contains("$"))
            {
                String[] indexed = step.split("\\$");
                Preconditions.checkState(indexed.length==2, "there should just be a $ sign at most when looking at optimization Parameters");
                node = PropertyUtils.getIndexedProperty(node, indexed[0], Integer.parseInt(indexed[1]));
            }
            else if(step.contains("~"))
            {
                String[] indexed = step.split("~");
                Preconditions.checkState(indexed.length==2, "there should just be a ~ sign at most when looking at optimization Parameters");
                node = PropertyUtils.getMappedProperty(node, indexed[0], indexed[1]);
            }
            else {
                node = PropertyUtils.getProperty(node, step);
            }
        }
        String finalStep = steps[steps.length - 1];

        if(finalStep.contains("$"))
        {
            String[] indexed = finalStep.split("\\$");
            Preconditions.checkState(indexed.length==2, "there should just be a $ sign at most when looking at optimization Parameters");
            PropertyUtils.setIndexedProperty(node, indexed[0], Integer.parseInt(indexed[1]),value);
        }
        else if(finalStep.contains("~"))
        {
            String[] indexed = finalStep.split("~");
            Preconditions.checkState(indexed.length==2, "there should just be a ~ sign at most when looking at optimization Parameters");
            PropertyUtils.setMappedProperty(node, indexed[0], indexed[1],value);
        }
        else {
            PropertyUtils.setProperty(node, finalStep, value);
        }




    }

}
