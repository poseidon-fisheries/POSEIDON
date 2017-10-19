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
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;

/**
 * Looks for DoubleParameter objects and tag them for processing
 * Created by carrknight on 6/7/15.
 */
public class DoubleParameterInspector extends BaseObjectInspector
{


    /**
     * if it finds a DoubleParameter it tags it appropriately
     */
    @Override
    protected Map<String, String> inspectProperty(Property property) throws Exception
    {

        Map<String, String> attributes = CollectionUtils.newHashMap();

        if(property.isWritable())
            try {
                final Class<?> propertyClass = Class.forName(property.getType());
                if (DoubleParameter.class.isAssignableFrom(propertyClass)) {
                    //it is a double parameter!
                    attributes.put(DoubleParameterWidgetProcessor.KEY_TO_LOOK_FOR,
                                   property.getType());
                }
            } catch (ClassNotFoundException e) {
                //this can happen (think primitives)
            }
        return attributes;

    }
}
