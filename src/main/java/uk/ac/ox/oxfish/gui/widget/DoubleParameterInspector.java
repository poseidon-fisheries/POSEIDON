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
