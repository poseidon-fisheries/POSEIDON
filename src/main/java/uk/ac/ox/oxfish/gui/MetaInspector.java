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

package uk.ac.ox.oxfish.gui;

import org.metawidget.inspector.composite.CompositeInspector;
import org.metawidget.inspector.composite.CompositeInspectorConfig;
import org.metawidget.inspector.propertytype.PropertyTypeInspector;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.swing.widgetbuilder.OverriddenWidgetBuilder;
import org.metawidget.swing.widgetbuilder.ReadOnlyWidgetBuilder;
import org.metawidget.swing.widgetbuilder.SwingWidgetBuilder;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessor;
import org.metawidget.swing.widgetprocessor.binding.beanutils.BeanUtilsBindingProcessorConfig;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilder;
import org.metawidget.widgetbuilder.composite.CompositeWidgetBuilderConfig;
import sim.portrayal.Inspector;
import uk.ac.ox.oxfish.gui.widget.*;

import javax.swing.*;

/**
 * A meta-widget implementation of the usual inspector. Makes modifying strategy parameters a lot easier
 * Created by carrknight on 6/7/15.
 */
public class MetaInspector extends Inspector
{

    final private Object toInspect;

    final private SwingMetawidget widget = new SwingMetawidget();

    public MetaInspector(Object toInspect, FishGUI gui) {
        this.toInspect = toInspect;


        STANDARD_WIDGET_SETUP(widget, gui);

        setVolatile(true); //volatile by default


        widget.setToInspect(toInspect);
        //widget.getWidgetProcessor( BeanUtilsBindingProcessor.class ).rebind(widget.getToInspect(), widget);
        this.add(widget);

    }


    @Override
    public void updateInspector() {
        widget.setToInspect(toInspect);
    }

    /**
     * Setup standard widget with the usual binders and post-processors
     * @param widget the widget to set up
     * @param toSyncAgainst nullable: a link to the gui-state if we need to sync
     *                      the writing to the model. It is also where we get the randomizer so it
     *                      is important whenever the model has already started
     */
    public static void STANDARD_WIDGET_SETUP(SwingMetawidget widget, FishGUI toSyncAgainst) {
        //create inspectors
        CompositeInspectorConfig inspectorConfig = new CompositeInspectorConfig().setInspectors(
                //default for swing
                new PropertyTypeInspector(),
                //adds information for selecting factories
                new StrategyFactoryInspector(),
                //tags already instantiated strategies. Useful only with GUI state
                new StrategyInspector(),
                //adds information on randomizable parameters
                new DoubleParameterInspector()
        );
        widget.setInspector(new CompositeInspector(inspectorConfig));
        //if you have a gui start with add the data widgets, so you can plot datasets on the fly
        if(toSyncAgainst != null)
        {
            CompositeWidgetBuilder<JComponent, SwingMetawidget> composite = new CompositeWidgetBuilder<>(
                    new CompositeWidgetBuilderConfig<JComponent, SwingMetawidget>().
                            setWidgetBuilders(
                                    new DataWidgetBuilder(toSyncAgainst),
                                    new MarketWidgetBuilder(toSyncAgainst),
                                    new GeographicalRegressionWidget(toSyncAgainst),
                                    new DiscretizationWidget(toSyncAgainst),
                                    new EnumWidgetBuilder(),
                                    new PathWidgetBuilder(),
                                    new HashMapWidgetBuilder(),
                                    new OverriddenWidgetBuilder(),
                                    new ReadOnlyWidgetBuilder(),
                                    new SwingWidgetBuilder()
                            )
            );
            widget.setWidgetBuilder(composite);
        }
        else{
            CompositeWidgetBuilder<JComponent, SwingMetawidget> composite = new CompositeWidgetBuilder<>(
                    new CompositeWidgetBuilderConfig<JComponent, SwingMetawidget>().
                            setWidgetBuilders(
                                    new EnumWidgetBuilder(),
                                    new PathWidgetBuilder(),
                                    new HashMapWidgetBuilder(),
                                    new OverriddenWidgetBuilder(),
                                    new ReadOnlyWidgetBuilder(),
                                    new SwingWidgetBuilder()
                            )
            );
            widget.setWidgetBuilder(composite);
        }


        //add the processor
        //this makes the binding between ui and model possible
        BeanUtilsBindingProcessorConfig config = new BeanUtilsBindingProcessorConfig();
        widget.addWidgetProcessor(new BeanUtilsBindingProcessor(config));
        //this one makes the binding immediate
        if(toSyncAgainst == null) {
            widget.addWidgetProcessor(new ImmediateBinder());
        }
        else {
            //this one makes binding almost immediate: waits for the guischedule
            widget.addWidgetProcessor(new GUISyncedBinder(toSyncAgainst));
            //with GUI state we get the randomizer and so we can swap strategies on the fly
            widget.addWidgetProcessor(new StrategyWidgetProcessor(toSyncAgainst));
        }
        // this one creates the combo-boxes for strategy factories
        widget.addWidgetProcessor(new StrategyFactoryWidgetProcessor());
        //creates combo-boxes for double parameters
        widget.addWidgetProcessor(new DoubleParameterWidgetProcessor());
        widget.addWidgetProcessor(new ArrayWidgetProcessor());



    }
}
