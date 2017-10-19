/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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
    public void writeToObject(SwingMetawidget metawidget, boolean rebind) {
        synchronized(gui.state.schedule) {
            super.writeToObject(metawidget, rebind);
        }
    }
}
