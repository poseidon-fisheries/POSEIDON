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

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import uk.ac.ox.oxfish.gui.DataCharter;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

/**
 * For every Dataset add a bunch of buttons to plot that column. Also do that for every Collection or Map or Maps of maps.
 * They could all be strictly different builders, but this way i lower the amount of useless code replication
 * Created by carrknight on 6/13/15.
 */
public class DataWidgetBuilder implements WidgetBuilder<JComponent,SwingMetawidget>{


    /**
     * needs the gui up and running for plotting to make sense
     */
    private final FishGUI gui;


    /**
     * creates buttons which spawn data plots
     * @param gui the gui running the model
     */
    public DataWidgetBuilder(FishGUI gui) {
        this.gui = gui;
    }

    /**
     * if the class is a TimeSeries, build a list of buttons
     */
    @Override
    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {

        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        if(actualClass == null)
            return null;

        try {
            if (TimeSeries.class.isAssignableFrom(actualClass)) {
                //create panel
                //todo figure out to make this nested

                return buildDataSetPanel((TimeSeries) PropertyUtils.getNestedProperty(metawidget.getToInspect(),
                                                                                      attributes.get("name")));

            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons!");
        }


        //now imagine it is a list of data-sets
        final String parametrizedType = attributes.get("parameterized-type");

        try {
            //if it is a collection
            if (Collection.class.isAssignableFrom(actualClass) && parametrizedType != null) {
                final Class<?> type = Class.forName(parametrizedType);
                if(TimeSeries.class.isAssignableFrom(type)) {
                    final Collection<TimeSeries> datasets = (Collection<TimeSeries>) PropertyUtils.getNestedProperty(
                            metawidget.getToInspect(),
                            attributes.get("name"));
                    //return a common tabbed pane
                    JTabbedPane many = new JTabbedPane();
                    int i=0;
                    for(TimeSeries d : datasets)
                        many.add("data :" + i++,buildDataSetPanel(d));
                    return many;
                }

            }

        } catch(ClassNotFoundException e){}
        catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons at " + elementName);
        }

        //now imagine it's a map of data-sets
        try {
            //if it is a map
            if (Map.class.isAssignableFrom(actualClass) && parametrizedType != null) {

                if(TimeSeries.class.isAssignableFrom(whatIsTheMapHolding(parametrizedType)))
                {
                    final Map<?, TimeSeries> datasets = (Map<?, TimeSeries>) PropertyUtils.getNestedProperty(
                            metawidget.getToInspect(),
                            attributes.get("name"));

                    return buildMapTabbedPane(datasets);
                }

            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons!");
        }

        //finally what if it is a map of maps?
        try {
            //if it is a map
            if (Map.class.isAssignableFrom(actualClass) && parametrizedType != null ) {
                //and it holds a map
                if(Map.class.isAssignableFrom(whatIsTheMapHolding(parametrizedType)))
                {
                    //grab everything inside the < >
                    String typeOfType = parametrizedType.substring(parametrizedType.indexOf("<"),
                                                                   parametrizedType.lastIndexOf(">") + 1);
                    //is that a valid map?
                    if(TimeSeries.class.isAssignableFrom(whatIsTheMapHolding(typeOfType)))
                    {
                        final Map<?,Map<?, TimeSeries>> datasets = (Map<?,Map<?, TimeSeries>>) PropertyUtils.getNestedProperty(
                                metawidget.getToInspect(),
                                attributes.get("name"));

                        //return a common tabbed pane
                        JTabbedPane many = new JTabbedPane();
                        int i=0;
                        for(Map.Entry<?, Map<?,TimeSeries>> md : datasets.entrySet())
                        {
                            many.add(buildMapTabbedPane(md.getValue()));
                        }
                        return many;
                    }
                }

            }

            return null;

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons!");
        }


        return null;
    }

    public JComponent buildMapTabbedPane(Map<?, TimeSeries> datasets) {
        //return a common tabbed pane
        JTabbedPane many = new JTabbedPane();
        int i=0;
        for(Map.Entry<?, TimeSeries> d : datasets.entrySet())
        {
            many.add(d.getKey().toString(),buildDataSetPanel(d.getValue()));
        }
        return many;
    }

    /**
     * gets what the map supposedly has as values. If it doesn't hold anything. Returns Object.class
     * @throws ClassNotFoundException
     */
    private Class whatIsTheMapHolding(String parametrizedType) throws ClassNotFoundException {
        if(parametrizedType == null)
            return Object.class;
        try {
            //notice here that i want the second type (it's a map) and I want it cleared of subtype

            String typeName = parametrizedType.split(",")[1];
            if (typeName == null || typeName.length() < 1)
                return Object.class;
            //remove subtype, if necessary
            typeName = typeName.replaceAll("<.*?> ?", "");
            //is it a map of datasets?
            return Class.forName(typeName);
        }
        catch (ArrayIndexOutOfBoundsException e){
            return Object.class;
        }
    }

    public JPanel buildDataSetPanel(final TimeSeries<?> dataset) {
        //grab the actual data
        //contains the key

        GridLayout layout = new GridLayout(0, 1);
        JPanel buttons = new JPanel(layout);

        for(DataColumn column : dataset.getColumns()){
            JButton columnButton = new JButton(column.getName());
            Box box = new Box(BoxLayout.X_AXIS);
            box.add(columnButton);

            //on click starts the chart
            columnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DataCharter charter = new DataCharter(dataset.getPolicy(),column);
                    charter.start(gui);
                }
            });
            //create button to output csv as well
            box.add(Box.createHorizontalGlue());
            JButton printOut = new JButton();
            printOut.setText("to csv");
            printOut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showSaveDialog(buttons) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        // save to file
                        FishStateUtilities.printCSVColumnToFile(file,column);
                    }
                }
            });
            box.add(printOut);

            //add it all to panel
            buttons.add(box);
        }
        return buttons;
    }


}
