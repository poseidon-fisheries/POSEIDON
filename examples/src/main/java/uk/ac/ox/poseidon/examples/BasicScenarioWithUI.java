/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.examples;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.gui.DisplayWrapper2D;
import uk.ac.ox.poseidon.gui.ScenarioWithUI;
import uk.ac.ox.poseidon.gui.portrayals.*;

import java.util.List;

import static java.awt.Color.WHITE;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.*;

public class BasicScenarioWithUI extends ScenarioWithUI {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 1000;

    public BasicScenarioWithUI(final BasicScenario scenario) {
        super(
            scenario,
            List.of(
                new DisplayWrapper2D(
                    "Ocean",
                    ImmutableMap.of(
                        "Bathymetry",
                        new DivergingNumberGridPortrayalFactory(
                            OLERON,
                            "Elevation",
                            true,
                            scenario.getBathymetricGrid()
                        ),
                        "Carrying capacity",
                        new NumberGridPortrayalFactory(
                            IMOLA,
                            "Carrying capacity",
                            true,
                            scenario.getCarryingCapacityGrid()
                        ),
                        "Species A Biomass",
                        new NumberGridWithCapacityPortrayalFactory(
                            LAJOLLA,
                            "Biomass",
                            false,
                            scenario.getBiomassGridA(),
                            scenario.getCarryingCapacityGrid()
                        ),
                        "Species B Biomass",
                        new NumberGridWithCapacityPortrayalFactory(
                            LAJOLLA,
                            "Biomass",
                            false,
                            scenario.getBiomassGridB(),
                            scenario.getCarryingCapacityGrid()
                        ),
                        "Ports",
                        new PortGridPortrayalFactory(scenario.getPortGrid()),
                        "Vessels",
                        new VesselFieldPortrayalFactory(scenario.getVesselField()),
                        "Regulations",
                        new RegulationGridPortrayalFactory(
                            scenario.getRegulations(),
                            scenario.getFleet(),
                            scenario.getBathymetricGrid()
                        )
                    ),
                    WIDTH,
                    HEIGHT,
                    WHITE
                )
            )
        );
    }

    public static void main(final String[] args) {
        final BasicScenarioWithUI basicScenarioWithUI =
            new BasicScenarioWithUI(new BasicScenario());
        basicScenarioWithUI.createController();
    }

}
