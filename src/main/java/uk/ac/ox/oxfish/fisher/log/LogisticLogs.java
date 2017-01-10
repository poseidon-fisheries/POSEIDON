package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.LinkedList;

/**
 * Collection of logistic log that can output to file.
 * Created by carrknight on 1/10/17.
 */
public class LogisticLogs extends LinkedList<LogisticLog> implements OutputPlugin {


    @Override
    public String getFileName() {
        return "logistic_wide.csv";
    }

    /**
     * create a "long-format" csv file by row-binding all the separate loggers
     * @return
     */
    @Override
    public String composeFileContents() {
        if(isEmpty())
            return  "";

        StringBuilder builder = new StringBuilder();
        builder.append(get(0).getColumnNames()).append("\n");
        for (LogisticLog log : this)
            builder.append(log.getData().toString()).append('\n');
        return builder.toString();

    }
}
