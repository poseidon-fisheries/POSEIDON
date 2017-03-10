package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.util.LinkedList;

/**
 * Collection of logistic log that can output to file.
 * Created by carrknight on 1/10/17.
 */
public class LogisticLogs extends LinkedList<LogisticLog> implements OutputPlugin {


    private String filename = "logistic_long.csv";

    @Override
    public String getFileName() {
        return filename;
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
            builder.append(log.getData().toString());
        return builder.toString();

    }




    /**
     * Setter for property 'filename'.
     *
     * @param filename Value to set for property 'filename'.
     */
    public void setFileName(String filename) {
        this.filename = filename;
    }
}
