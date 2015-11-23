package uk.ac.ox.oxfish.utility.yaml;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

/**
 * A simple hash-map that gets created at the end of the model to collect FishState information
 * Created by carrknight on 11/20/15.
 */
public class ModelResults extends HashMap<String,Object>
{


    /**
     * Construct the hash-map. Ready for serialization!
     */
    public ModelResults(FishState state) {


        //dump columns of yearly data in
        HashMap<String, Object> modelData = new HashMap<>(state.getYearlyDataSet().getColumns().size());
        for (DataColumn column : state.getYearlyDataSet().getColumns())
            modelData.put(column.getName(), column.copy());
        this.put("FishState", modelData);

        //Also prints out fisher's averages
        HashMap<String, Object> fisherData = new HashMap<>();
        Collection<DataColumn> fisherColumns = state.getFishers().get(0).getYearlyData().getColumns();
        int years = fisherColumns.iterator().next().size();
        for (DataColumn column : fisherColumns)
        {
            HashMap<String, Object> columnData = new HashMap<>();
            fisherData.put(column.getName(),columnData);

            for(int year = 0; year<years; year++)
            {

                DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
                for(Fisher fisher : state.getFishers())
                    stats.accept(fisher.getYearlyData().getColumn(column.getName()).get(year));
                HashMap<String,Object> yearData = new HashMap<>();
                columnData.put(Integer.toString(year),yearData);
                yearData.put("Average",stats.getAverage());
                yearData.put("Max",stats.getMax());
                yearData.put("Min",stats.getMin());
                yearData.put("Count",stats.getCount());
            }

        }
        this.put("Fisher",fisherData);
    }

}
