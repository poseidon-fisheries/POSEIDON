package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.collect.ImmutableList;
import org.apache.commons.beanutils.PropertyUtils;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class ParameterAddress {
    private final String address;

    public ParameterAddress(final String address) {
        checkArgument(!checkNotNull(address).isEmpty());
        this.address = address;
    }

    private static Entry<String, String> splitBy(final String s, final String sep) {
        final String[] strings = s.split(Pattern.quote(sep));
        checkArgument(
            strings.length == 2,
            "There should be one and only one % in the string %", sep, s
        );
        return entry(strings[0], strings[1]);
    }

    public Supplier<Object> getGetter(final Scenario scenario) {
        return getProperty(scenario).getGetter();
    }

    public Property getProperty(final Scenario scenario) {
        return getProperty(scenario, ImmutableList.copyOf(this.address.split("\\.")));
    }

    private Property getProperty(final Object object, final List<String> address) {
        checkArgument(!address.isEmpty());
        final String head = address.get(0);
        final Property property;
        if (head.contains("$")) {
            final Entry<String, String> entry = splitBy(head, "$");
            property = new IndexedProperty(object, entry.getKey(), Integer.parseInt(entry.getValue()));
        } else if (head.contains("~")) {
            final Entry<String, String> entry = splitBy(head, "~");
            property = new MappedProperty(object, entry.getKey(), entry.getValue());
        } else {
            property = new Property(object, head);
        }
        return address.size() == 1
            ? property
            : getProperty(property.getGetter().get(), address.subList(1, address.size()));
    }

    public Consumer<Object> getSetter(final Scenario scenario) {
        return getProperty(scenario).getSetter();
    }

    private static class Property {
        private final Object bean;
        private final String name;

        private Property(final Object bean, final String name) {
            this.bean = bean;
            this.name = name;
        }

        public Object getBean() {
            return bean;
        }

        public String getName() {
            return name;
        }

        public Supplier<Object> getGetter() {
            return () -> {
                try {
                    return PropertyUtils.getProperty(bean, name);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        public Consumer<Object> getSetter() {
            return (value) -> {
                try {
                    PropertyUtils.setProperty(bean, name, value);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private static class IndexedProperty extends Property {
        private final int index;

        private IndexedProperty(final Object bean, final String name, final int index) {
            super(bean, name);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public Supplier<Object> getGetter() {
            return () -> {
                try {
                    return PropertyUtils.getIndexedProperty(getBean(), getName(), index);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        @Override
        public Consumer<Object> getSetter() {
            return (value) -> {
                try {
                    PropertyUtils.setIndexedProperty(getBean(), getName(), index, value);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private static class MappedProperty extends Property {
        private final String key;

        private MappedProperty(final Object bean, final String name, final String key) {
            super(bean, name);
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public Supplier<Object> getGetter() {
            return () -> {
                try {
                    return PropertyUtils.getMappedProperty(getBean(), getName(), key);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        @Override
        public Consumer<Object> getSetter() {
            return (value) -> {
                try {
                    PropertyUtils.setMappedProperty(getBean(), getName(), key, value);
                } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

}
