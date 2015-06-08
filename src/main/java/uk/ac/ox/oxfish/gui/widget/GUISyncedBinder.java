package uk.ac.ox.oxfish.gui.widget;

import org.metawidget.swing.SwingMetawidget;
import sim.display.GUIState;

/**
 * Like an immediate binder but has a link to the GUIState to sync against
 * because updateInspector() doesn't really pass GUIState as it should
 * Created by carrknight on 6/7/15.
 */
public class GUISyncedBinder extends ImmediateBinder
{

    private final GUIState gui;

    public GUISyncedBinder(GUIState gui) {
        this.gui = gui;
    }

    @Override
    public void writeToObject(SwingMetawidget metawidget) {
        synchronized(gui.state.schedule) {
            super.writeToObject(metawidget);
        }
    }
}
