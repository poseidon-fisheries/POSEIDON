package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.opencsv.CSVWriter;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.OutputPlugin;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * adapting row providers to play nicely with running the model without the runner
 */
public class RowProviderToOutputPluginAdaptor implements OutputPlugin, AdditionalStartable {


    private final RowProvider delegate;

    private final String filename;


    public RowProviderToOutputPluginAdaptor(RowProvider delegate, String filename) {
        this.delegate = delegate;
        this.filename = filename;
    }

    @Override
    public void reactToEndOfSimulation(FishState state) {
        //nothing much, row provider never needs to wait till the end
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public String composeFileContents() {
        //row provider splits them, so we got to put them back together
        StringBuilder builder = new StringBuilder();
        // if there are headers...
        Iterator<String> headers = delegate.getHeaders().iterator();
        while(headers.hasNext())
        {
            builder.append(headers.next());
            if(headers.hasNext())
                builder.append(",");
        }
        builder.append("\n");

        for (Collection<?> row : delegate.getRows()) {
            Iterator<?> rowElements = row.iterator();
            while(rowElements.hasNext())
            {
                builder.append(rowElements.next());
                if(rowElements.hasNext())
                    builder.append(",");
            }

            builder.append("\n");
        }


        return builder.toString();
    }

    @Override
    public void start(FishState model) {
        //this is how the runner does it, so let's do it here too
        if(delegate instanceof Startable)
            ((Startable) delegate).start(model);
        model.getOutputPlugins().add(this);
    }

    @Override
    public void turnOff() {
        if(delegate instanceof Startable)
            ((Startable) delegate).turnOff();
    }
}
