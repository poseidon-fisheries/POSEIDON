/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.examples.petersnapper;

import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.gui.DisplayWrapper2D;
import uk.ac.ox.poseidon.gui.ScenarioWithUI;
import uk.ac.ox.poseidon.gui.portrayals.*;

import java.util.List;

import static java.awt.Color.WHITE;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.IMOLA;

public class PeterSnapperScenarioWithUI extends ScenarioWithUI {

    public PeterSnapperScenarioWithUI(
        final Scenario scenario
    ) {
        super(
            scenario,
            List.of(
                new DisplayWrapper2D(
                    "The Peter Snapper Fishery",
                    List.of(
                        new BathymetryFieldPortrayalFactory(
                            scenario.component("bathymetricGrid")
                        ),
                        new SimpleFieldPortrayalFactory(
                            "Carrying capacity",
                            new NumberGridPortrayalFactory(
                                IMOLA,
                                "Carrying capacity",
                                true,
                                scenario.component("carryingCapacityGrid")
                            ),
                            false
                        ),
                        new SpeciesBiomassFieldPortrayalFactory(
                            scenario.component("biomassGrid"),
                            scenario.component("carryingCapacityGrid"),
                            false
                        ),
                        new SimpleFieldPortrayalFactory(
                            "Markets",
                            new MarketGridPortrayalFactory(
                                scenario.component("marketGrid")
                            ),
                            true
                        ),
                        new SimpleFieldPortrayalFactory(
                            "Ports",
                            new PortGridPortrayalFactory(
                                scenario.component("portGrid")
                            ),
                            true
                        ),
                        new SimpleFieldPortrayalFactory(
                            "Vessels",
                            new VesselFieldPortrayalFactory(
                                scenario.component("vesselField")
                            ),
                            true
                        ),
                        new SimpleFieldPortrayalFactory(
                            "Coordinates",
                            new CoordinatesPortrayalFactory(
                                scenario.component("modelGrid"),
                                3
                            ),
                            true
                        )
                    ),
                    700,
                    320,
                    WHITE
                )
            )
        );
    }

    public static void main(final String[] args) {
        final var scenarioWithUI = new PeterSnapperScenarioWithUI(new PeterSnapperScenario().get());
        scenarioWithUI.createController();
    }
}

