package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;

/**
 * The logistic log keeps track of the decisions made by a logit multi classifier (or as if one was taking these
 * decisions) to output
 * Created by carrknight on 12/9/16.
 */
public class LogisticLog {


    final private String columnNames ;
    final private StringBuilder data = new StringBuilder();

    final private int id;

    private double[][] lastInput;

    private int episode = 0;

    public LogisticLog(final String[] columnNames, int id)
    {
        this.id = id;
        StringBuilder columns = new StringBuilder();
        columns.append("id,episode,option,choice");
        for(String column : columnNames)
            columns.append(",").append(column);
        this.columnNames = columns.toString();
    }

    public void recordInput(double[][] x)
    {
        Preconditions.checkArgument(lastInput == null, "haven't closed last trip");
        lastInput = x;
    }

    public void recordChoice(int choice)
    {
        Preconditions.checkArgument(lastInput != null, "don't have a matching input!");
        for(int arm= 0; arm< lastInput.length; arm++)
        {
            data.append(id).append(",").
                    append(episode).append(",").
                    append(arm).append(",").
                    append(choice==arm ? "yes" : "no");
            for(int i=0; i<lastInput[arm].length; i++)
                data.append(",").append(lastInput[arm][i]);
            data.append("\n");
        }
        episode++;
        lastInput = null;
    }


    /**
     * Getter for property 'data'.
     *
     * @return Value for property 'data'.
     */
    public StringBuilder getData() {
        return data;
    }

    public String getColumnNames() {
        return columnNames;
    }
}
