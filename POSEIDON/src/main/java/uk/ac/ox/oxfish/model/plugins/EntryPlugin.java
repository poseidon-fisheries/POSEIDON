package uk.ac.ox.oxfish.model.plugins;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;


/**
 * almost a "marker" interface for any object that creates fishers automatically (due to profits and so on).
 * Makes sure that we can turn off the entry externally which is something that some regulations require
 */
public interface EntryPlugin extends Steppable, AdditionalStartable {


    public boolean isEntryPaused();

    public void setEntryPaused(boolean entryPaused);


}
