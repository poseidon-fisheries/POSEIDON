/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.poseidon.simulations.adaptors;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.beanutils.ConvertUtils;
import uk.ac.ox.oxfish.maximization.generic.BeanParameterAddressBuilder;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.parameters.ParameterExtractor;
import uk.ac.ox.oxfish.utility.parameters.BooleanParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.AbstractAdaptor;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.PathParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.simulations.api.Parameter;
import uk.ac.ox.poseidon.simulations.api.Scenario;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class ScenarioAdaptor
    extends AbstractAdaptor<uk.ac.ox.oxfish.model.scenario.Scenario>
    implements Scenario {

    ScenarioAdaptor(final uk.ac.ox.oxfish.model.scenario.Scenario delegate) {
        super(delegate);
    }

    @Override
    public Map<String, uk.ac.ox.poseidon.simulations.api.Parameter> getParameters() {
        return new ParameterExtractor(
            ImmutableSet.of(
                uk.ac.ox.poseidon.common.api.parameters.Parameter.class,
                Number.class,
                Boolean.class,
                String.class
            ),
            BeanParameterAddressBuilder::new
        ).getParameters(getDelegate())
            .map(extractedParameter -> {
                final Object parameter = extractedParameter.getObject();
                if (parameter instanceof DoubleParameter)
                    return new DoubleParameterAdaptor(
                        (DoubleParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof PathParameter)
                    return new PathParameterAdaptor(
                        (PathParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof IntegerParameter)
                    return new IntegerParameterAdaptor(
                        (IntegerParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof BooleanParameter)
                    return new BooleanParameterAdaptor(
                        (BooleanParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else if (parameter instanceof StringParameter)
                    return new StringParameterAdaptor(
                        (StringParameter) parameter,
                        extractedParameter.getAddress(),
                        this.getDelegate()
                    );
                else return new BeanParameter<>(
                        this.getDelegate(),
                        extractedParameter.getAddress(),
                        parameter.getClass(),
                        ConvertUtils.lookup(parameter.getClass())
                    );
            })
            .collect(toImmutableMap(Parameter::getName, identity()));
    }

    @Override
    public Simulation newSimulation() {
        final FishState fishState = new FishState();
        fishState.setScenario(getDelegate());
        return new FishStateAdaptor(fishState);
    }
}
