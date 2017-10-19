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

import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.util.CollectionUtils;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Marks down all the properties who are strategies. Useful for postprocessing
 * Created by carrknight on 6/8/15.
 */
public class StrategyInspector extends BaseObjectInspector
{

    public static final String KEY = "strategy_instance";

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
    protected Map<String, String> inspectProperty(Property property) throws Exception {


        Map<String, String> attributes = CollectionUtils.newHashMap();

        if(property.isWritable())
        {
            try {
                //get property class
                final Class<?> propertyClass = Class.forName(property.getType());
                //strategy classes
                final Set<Class> strategyClasses = AlgorithmFactories.CONSTRUCTOR_MAP.keySet();
                final Optional<Class> superclass = strategyClasses.stream().filter(
                        aclass -> aclass.isAssignableFrom(propertyClass)).findFirst();
                superclass.ifPresent(aClass -> attributes.put(KEY, aClass.getName()));
            }catch (ClassNotFoundException e) {
                //this can happen (think primitives)
            }

        }
        return attributes;



    }
}
