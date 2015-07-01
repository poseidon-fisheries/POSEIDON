package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;
import uk.ac.ox.oxfish.gui.DataCharter;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.data.DataColumn;
import uk.ac.ox.oxfish.model.data.DataSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
     * if the class is a DataSet, build a list of buttons
     */
    @Override
    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {

        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        if(actualClass == null)
            return null;

        try {
            if (DataSet.class.isAssignableFrom(actualClass)) {
                System.out.println("it's a dataset!'");
                //create panel
                //todo figure out to make this nested

                return buildDataSetPanel((DataSet) PropertyUtils.getNestedProperty(metawidget.getToInspect(),
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
                if(DataSet.class.isAssignableFrom(type)) {
                    System.out.println("it's a collection of datasets!");
                    final Collection<DataSet> datasets = (Collection<DataSet>) PropertyUtils.getNestedProperty(
                            metawidget.getToInspect(),
                            attributes.get("name"));
                    //return a common tabbed pane
                    JTabbedPane many = new JTabbedPane();
                    int i=0;
                    for(DataSet d : datasets)
                        many.add("data :" + i++,buildDataSetPanel(d));
                    return many;
                }

            }
        } catch (InvocationTargetException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons!");
        }

        //now imagine it's a map of data-sets
        try {
            //if it is a map
            if (Map.class.isAssignableFrom(actualClass) && parametrizedType != null) {

                if(DataSet.class.isAssignableFrom(whatIsTheMapHolding(parametrizedType)))
                {
                    final Map<?, DataSet> datasets = (Map<?, DataSet>) PropertyUtils.getNestedProperty(
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
                    if(DataSet.class.isAssignableFrom(whatIsTheMapHolding(typeOfType)))
                    {
                        final Map<?,Map<?, DataSet>> datasets = (Map<?,Map<?, DataSet>>) PropertyUtils.getNestedProperty(
                                metawidget.getToInspect(),
                                attributes.get("name"));

                        //return a common tabbed pane
                        JTabbedPane many = new JTabbedPane();
                        int i=0;
                        for(Map.Entry<?, Map<?,DataSet>> md : datasets.entrySet())
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

    public JComponent buildMapTabbedPane(Map<?, DataSet> datasets) {
        //return a common tabbed pane
        JTabbedPane many = new JTabbedPane();
        int i=0;
        for(Map.Entry<?, DataSet> d : datasets.entrySet())
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

    public JPanel buildDataSetPanel(final DataSet<?> dataset) {
        //grab the actual data
        //contains the key

        GridLayout layout = new GridLayout(0, 1);
        JPanel buttons = new JPanel(layout);

        for(DataColumn column : dataset.getColumns()){
            JButton columnButton = new JButton(column.getName());
            //add it to panel
            buttons.add(columnButton);
            //on click starts the chart
            columnButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DataCharter charter = new DataCharter(dataset.getPolicy(),column);
                    charter.start(gui);
                }
            });
        }
        return buttons;
    }


}
