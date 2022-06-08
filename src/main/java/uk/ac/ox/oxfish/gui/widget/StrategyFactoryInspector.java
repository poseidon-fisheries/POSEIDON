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

package uk.ac.ox.oxfish.gui.widget;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.inspector.impl.propertystyle.javabean.JavaBeanPropertyStyle.JavaBeanProperty;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * The "MetaInspector" (in the metawidget sense, not the mason sense) that looks for
 * AlgorithmFactories. If it finds one it adds a "factory" attribute to the element Created by
 * carrknight on 5/29/15.
 */
public class StrategyFactoryInspector extends BaseObjectInspector {

    /**
     * Inspect the given property and return a Map of attributes.
     * <p>
     * Note: for convenience, this method does not expect subclasses to deal with DOMs and Elements.
     * Those subclasses wanting more control over these features should override methods higher in
     * the call stack instead.
     * <p>
     * Does nothing by default.
     *
     * @param property the property to inspect
     */
    @Override
    protected Map<String, String> inspectProperty(final Property property) {
        final Map<String, String> attributes = new HashMap<>();
        if (property.isWritable()) {
            Type type = ((JavaBeanProperty) property)
                .getWriteMethod()
                .getGenericParameterTypes()[0];
            if (isAlgorithmFactory(type)) {
                if (type instanceof ParameterizedType && type.getTypeName().contains("AlgorithmFactory")) {
                    type = ((ParameterizedType) type).getActualTypeArguments()[0];
                    if (type instanceof WildcardType) {
                        type = ((WildcardType) type).getUpperBounds()[0];
                    }
                }
                type = rawType(type);
                attributes.put("factory_strategy", type.getTypeName());
            }
        }
        return attributes;
    }

    private static boolean isAlgorithmFactory(final Type type) {
        final Type rawType = rawType(type);
        return rawType instanceof Class<?>
            && AlgorithmFactory.class.isAssignableFrom((Class<?>) rawType);
    }

    private static Type rawType(final Type type) {
        return type instanceof ParameterizedType
            ? ((ParameterizedType) type).getRawType()
            : type;
    }

}
