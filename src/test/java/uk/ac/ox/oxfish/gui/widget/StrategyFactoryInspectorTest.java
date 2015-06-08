package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import static org.junit.Assert.*;

import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.w3c.dom.Element;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.FixedFavoriteDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomFavoriteDestinationFactory;
import uk.ac.ox.oxfish.utility.StrategyFactory;

/**
 * Created by carrknight on 5/29/15.
 */
public class StrategyFactoryInspectorTest {


    @Test
    public void readsCorrectly() throws Exception {

        //read serialize, make sure the StrategyFactoryInspector was fired
        ToSerialize serialize = new ToSerialize();
        CompositeInspectorConfig inspectorConfig = new CompositeInspectorConfig().setInspectors(
                new PropertyTypeInspector(),
                new StrategyFactoryInspector() );
        final CompositeInspector inspector = new CompositeInspector(inspectorConfig);
        //inspect!
        String attributeMap = inspector.inspect(serialize, serialize.getClass().getTypeName());
        //example of what should be in: factory_strategy="uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy"
        String lookingFor = "factory_strategy=\"" + DestinationStrategy.class.getName()+"\"";
        assertTrue(attributeMap.contains(lookingFor));

        //it should also work for serialize2 (that is, it shouldn't be fooled by the current value)
        ToSerialize2 two = new ToSerialize2();
        attributeMap = inspector.inspect(two, two.getClass().getTypeName());
        assertTrue(attributeMap.contains(lookingFor));

    }
}


/**
 * this class exists only so that we can make sure the MetaInspector reads  correctly.
 */
class ToSerialize
{
    /**
     * make sure this doesn't break anything!
     */
    int redHerring = 1;

    /**
     * a factory
     */
    StrategyFactory<? extends DestinationStrategy> firstFactory = new RandomFavoriteDestinationFactory();

    public int getRedHerring() {
        return redHerring;
    }

    public void setRedHerring(int redHerring) {
        this.redHerring = redHerring;
    }

    public StrategyFactory<? extends DestinationStrategy> getFirstFactory() {
        return firstFactory;
    }

    public void setFirstFactory(
            StrategyFactory<? extends DestinationStrategy> firstFactory) {
        this.firstFactory = firstFactory;
    }
}

class ToSerialize2
{
    /**
     * make sure this doesn't break anything!
     */
    int redHerring = 1;

    /**
     * a customized factory
     */
    StrategyFactory<? extends DestinationStrategy> firstFactory = new FixedFavoriteDestinationFactory();

    public int getRedHerring() {
        return redHerring;
    }

    public void setRedHerring(int redHerring) {
        this.redHerring = redHerring;
    }

    public StrategyFactory<? extends DestinationStrategy> getFirstFactory() {
        return firstFactory;
    }

    public void setFirstFactory(
            StrategyFactory<? extends DestinationStrategy> firstFactory) {
        this.firstFactory = firstFactory;
    }
}

