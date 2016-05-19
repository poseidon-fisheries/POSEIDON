package uk.ac.ox.oxfish.gui.widget;

import com.esotericsoftware.minlog.Log;
import org.apache.commons.beanutils.PropertyUtils;
import org.metawidget.swing.SwingMetawidget;
import org.metawidget.util.WidgetBuilderUtils;
import org.metawidget.widgetbuilder.iface.WidgetBuilder;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Transform an HashMap into a JTable
 * Created by carrknight on 5/4/16.
 */
public class HashMapWidgetBuilder implements WidgetBuilder<JComponent,SwingMetawidget>
{

    /**
     * Builds the most appropriate widget for this business field.
     *
     * @param elementName XML node name of the business field. Typically 'entity', 'property' or 'action'.
     *                    Never null
     * @param attributes  attributes of the business field to build a widget for. Never null. This Map is
     *                    modifiable - changes will be passed to subsequent WidgetBuilders, WidgetProcessors
     *                    and Layouts
     * @param metawidget  the parent Metawidget. Never null
     * @return the built widget. To suppress a field, return a Stub. To defer to the next
     * WidgetBuilder in the pipeline, return null. If there are no more WidgetBuilders in
     * the pipeline, will create a nested Metawidget. This approach (return Stub for no
     * field, null for nested Metawidget) as opposed to the other way around (return null
     * for no field, return Metawidget for nested Metawidget) works better for those UI
     * frameworks that cannot instatiate widgets without adding them to containers (eg. SWT)
     */
    @Override
    public JComponent buildWidget(
            String elementName, Map<String, String> attributes, SwingMetawidget metawidget) {
        final Class<?> actualClass = WidgetBuilderUtils.getActualClassOrType(attributes, String.class);
        if(actualClass == null || !Map.class.isAssignableFrom(actualClass))
            return null;
        final String[] path = metawidget.getPath().split("/");
        //nested address? no problem
        final String address = path.length == 2? path[1] + "." + attributes.get("name") :
                attributes.get("name");

        try {
            final Map inspected  = ((Map) PropertyUtils.getProperty(
                    //the container: the scenario probably
                    metawidget.getToInspect(),
                    //the address of the field
                    address
            ));

            JTable table = new JTable(new MapToTable<>(inspected));
            table.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    //use the beansutils to set the new value to the field

                    //so i bind it again by setter
                    metawidget.setToInspect(inspected);
                    if(metawidget.getParent()!=null) {
                        metawidget.getParent().revalidate();

                    }


                }
            });





            return table;

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.warn("failed to build JTable for "  + address);
            e.printStackTrace();
        }

        return null;





    }

    private static class MapToTable<K,V> extends AbstractTableModel
    {

        private final Map<K,V> delegate;

        public MapToTable(Map<K, V> delegate) {
            this.delegate = delegate;
        }

        /**
         * Returns the number of rows in the model. A
         * <code>JTable</code> uses this method to determine how many rows it
         * should display.  This method should be quick, as it
         * is called frequently during rendering.
         *
         * @return the number of rows in the model
         * @see #getColumnCount
         */
        @Override
        public int getRowCount() {
            return delegate.size();
        }

        /**
         * Returns the number of columns in the model. A
         * <code>JTable</code> uses this method to determine how many columns it
         * should create and display by default.
         *
         * @return the number of columns in the model
         * @see #getRowCount
         */
        @Override
        public int getColumnCount() {
            return 2;
        }

        /**
         * Returns the name of the column at <code>columnIndex</code>.  This is used
         * to initialize the table's column header name.  Note: this name does
         * not need to be unique; two columns in a table can have the same name.
         *
         * @param columnIndex the index of the column
         * @return the name of the column
         */
        @Override
        public String getColumnName(int columnIndex) {
            if(columnIndex==0)
                return "Key";
            else
                return "Value";
        }

        /**
         * Returns the most specific superclass for all the cell values
         * in the column.  This is used by the <code>JTable</code> to set up a
         * default renderer and editor for the column.
         *
         * @param columnIndex the index of the column
         * @return the common ancestor class of the object values in the model.
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {

            return String.class;
        }

        /**
         * Returns true if the cell at <code>rowIndex</code> and
         * <code>columnIndex</code>
         * is editable.  Otherwise, <code>setValueAt</code> on the cell will not
         * change the value of that cell.
         *
         * @param rowIndex    the row whose value to be queried
         * @param columnIndex the column whose value to be queried
         * @return true if the cell is editable
         * @see #setValueAt
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and
         * <code>rowIndex</code>.
         *
         * @param row    the row whose value is to be queried
         * @param column the column whose value is to be queried
         * @return the value Object at the specified cell
         */
        @Override
        public Object getValueAt(int row, int column) {
            Object[] entries=delegate.entrySet().toArray();
            Map.Entry entry=(Map.Entry)entries[row];
            if (column==0) {
                return entry.getKey();
            } else if (column==1) { // column==1
                return entry.getValue();
            } else {
                throw new IndexOutOfBoundsException("MapTableModel provides a 2-column table, column-index "+column+" is illegal.");
            }
        }
        /**
         * Sets the value in the cell at <code>columnIndex</code> and
         * <code>rowIndex</code> to <code>aValue</code>.
         *
         * @param aValue      the new value
         * @param rowIndex    the row whose value is to be changed
         * @param columnIndex the column whose value is to be changed
         * @see #getValueAt
         * @see #isCellEditable
         */
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            K oldKey = (K) getValueAt(rowIndex, 0);
            if(columnIndex==1) {
                delegate.put(oldKey, ((V) aValue));
            }
            else{
                V oldValue = delegate.remove(oldKey);
                assert oldValue!=null;
                delegate.put(((K) aValue),oldValue);
            }
        }


    }


}
