package uk.ac.ox.oxfish.gui.widget;

import org.apache.commons.beanutils.PropertyUtils;
import org.jfree.data.general.Dataset;
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
 * For every Dataset add a bunch of buttons to plot that column
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


        try {
            if (DataSet.class.isAssignableFrom(actualClass)) {
                System.out.println("it's a dataset!'");
                //create panel
                //todo figure out to make this nested

                return buildDataSetPanel((DataSet) PropertyUtils.getProperty(metawidget.getToInspect(),
                                                                                attributes.get("name")));

            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            System.err.println("failed to instantiate charting buttons!");
        }


        //now imagine it is a list of data-sets
        try {
            //if it is a collection
            if (Collection.class.isAssignableFrom(actualClass)) {
                final Class<?> type = Class.forName(attributes.get("parameterized-type"));
                if(DataSet.class.isAssignableFrom(type)) {
                    System.out.println("it's a collection of datasets!");
                    final Collection<DataSet> datasets = (Collection<DataSet>) PropertyUtils.getProperty(
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


        return null;
    }

    public JPanel buildDataSetPanel(final DataSet<?> dataset) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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
