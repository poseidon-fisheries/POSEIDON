package uk.ac.ox.oxfish.fisher.log.initializers;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.function.Consumer;

/**
 * Any object whose job is to create "logbook" data of some kind. As of this writing the only kind of logbook
 * I can think of is for logistic fits  but maybe other kinds might be possible.
 * This object is not to write the logbook itself rather it is to set up all the objects that do as the model runs
 *
 * Created by carrknight on 2/17/17.
 */
public interface LogbookInitializer extends Startable
{



    void add(Fisher fisher, FishState state);




}
