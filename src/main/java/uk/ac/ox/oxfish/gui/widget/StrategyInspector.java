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
