package uk.ac.ox.poseidon.examples;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.gui.DisplayWrapper2D;
import uk.ac.ox.poseidon.gui.ScenarioWithUI;
import uk.ac.ox.poseidon.gui.portrayals.*;

import java.util.List;

import static java.awt.Color.WHITE;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.*;

public class BasicScenarioWithUI extends ScenarioWithUI {

    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;

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
                        new VesselFieldPortrayalFactory(scenario.getVesselField())
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
