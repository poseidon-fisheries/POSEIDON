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

import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.core.MappedFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.gui.DisplayWrapper2D;
import uk.ac.ox.poseidon.gui.ScenarioWithUI;
import uk.ac.ox.poseidon.gui.portrayals.*;

import java.util.List;

import static java.awt.Color.WHITE;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.IMOLA;

public class WesternMedScenarioWithUI extends ScenarioWithUI {

    private static final int WIDTH = 1090;
    private static final int HEIGHT = 820;

    public WesternMedScenarioWithUI(final Scenario scenario) {
        super(
            scenario,
            List.of(
                new DisplayWrapper2D(
                    "Catalan Mediterranean Sea",
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
                        new MappedFactory<>(
                            scenario.<List<BiomassGrid>>component("biomassGrids"),
                            new SpeciesBiomassFieldPortrayalFactory(
                                null,
                                scenario.component("carryingCapacityGrid"),
                                false
                            ),
                            "biomassGrid"
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
                            "Regulations",
                            new RegulationGridPortrayalFactory(
                                scenario.component("regulations"),
                                scenario.component("fleet"),
                                scenario.component("bathymetricGrid"),
                                WIDTH,
                                HEIGHT
                            ),
                            true
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
        final WesternMedScenarioWithUI westernMedScenarioWithUI =
            new WesternMedScenarioWithUI(new WesternMedScenario().get());
        westernMedScenarioWithUI.createController();
    }

}
