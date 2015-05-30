package uk.ac.ox.oxfish.gui.widget;

import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessor;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessorConfig;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import javax.swing.*;

/**
 * The metawidget created by inspecting a scenario object. The name itself is a misnomer because
 * the JComponent (the widget) is actually a field rather than the class itself, but the use it the same:
 * point it to a scenario, it will generate all the gui you can access by getting the JComponent.
 *
 * <p>
 *     Notice that the scenario component is easier in general because the model isn't running while its fields
 *     are modified
 * </p>
 * Created by carrknight on 5/29/15.
 */
public class ScenarioJComponent {

    private final SwingMetawidget widget = new SwingMetawidget();

    private final Scenario scenario;

    /**
     * builds the gui: get the component with the getter
     * @param scenario the scenario to build the jcomponent for
     */
    public ScenarioJComponent(Scenario scenario) {
        this.scenario = scenario;

        //create inspectors
        CompositeInspectorConfig inspectorConfig = new CompositeInspectorConfig().setInspectors(
                //default for swing
                new PropertyTypeInspector(),
                //adds information for selecting factories
                new StrategyFactoryInspector()
        );
        widget.setInspector(new CompositeInspector(inspectorConfig));
        //add the processor
        //this makes the binding between ui and model possible
        widget.addWidgetProcessor(new BeanUtilsBindingProcessor(new BeanUtilsBindingProcessorConfig()));
        //this one makes the binding immediate
        widget.addWidgetProcessor(new ImmediateBinder());
        // this one creates the combo-boxes for strategy factories
        widget.addWidgetProcessor(new StrategyWidgetProcessor());

        widget.setToInspect(scenario);


    }

     public JComponent getJComponent(){
         return widget;
     }
}
