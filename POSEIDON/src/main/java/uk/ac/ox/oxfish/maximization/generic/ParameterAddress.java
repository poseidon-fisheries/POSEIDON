package uk.ac.ox.oxfish.maximization.generic;

import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ParameterAddress {
    private final String address;

    public ParameterAddress(final String address) {
        checkArgument(!checkNotNull(address).isEmpty());
        this.address = address;
    }

    public Object getValue(final Scenario scenario) {
        try {
            return PropertyUtils.getProperty(scenario, address);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(
        final Scenario scenario,
        final Object value
    ) {
        try {
            PropertyUtils.setProperty(scenario, address, value);
        } catch (
            final IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new RuntimeException(e);
        }
    }

}
