package uk.ac.ox.oxfish.gui.widget;

import org.metawidget.inspector.impl.BaseObjectInspector;
import org.metawidget.inspector.impl.propertystyle.Property;
import org.metawidget.util.CollectionUtils;
import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.Map;

/**
 * The "inspector" (in the metawidget sense, not the mason sense) that looks for StrategyFactories.
 * If it finds one it adds a "factory" attribute to the element
 * Created by carrknight on 5/29/15.
 */
public class StrategyFactoryInspector  extends BaseObjectInspector
{


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



        System.out.println(property);
        //turn String into Class object, if possible
        if(property.isWritable()) {
            try {
                final Class<?> propertyClass = Class.forName(property.getType());
                if (StrategyFactory.class.isAssignableFrom(propertyClass)) {
                    //it is a strategy factory!
                    //now most of the time it should be something like factory<? extends x>
                    //with getGenericType() we get ? extends x, but we want only x
                    //so we split and take last
                    final String[] splitType = property.getGenericType().split(" ");
                    //store it as attribute factory_strategy="x" which we will use to build widgets on
                    attributes.put("factory_strategy", splitType[splitType.length - 1]);
                }
            } catch (ClassNotFoundException e) {
                //this can happen (think primitives)
            }
        }
        return attributes;

    }
}
