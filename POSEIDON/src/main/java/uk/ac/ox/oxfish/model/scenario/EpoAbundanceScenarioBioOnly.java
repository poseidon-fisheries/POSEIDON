/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.BiologicalProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadMapFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.LinearAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.pathfinding.AStarFallbackPathfinder;
import uk.ac.ox.oxfish.maximization.TunaCalibrator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.maximization.TunaCalibrator.logCurrentTime;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoAbundanceScenarioBioOnly extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private AlgorithmFactory<? extends FadInitializer>
        fadInitializerFactory =
        new LinearAbundanceFadInitializerFactory(
            "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
        );

    public EpoAbundanceScenarioBioOnly() {
        setBiologicalProcessesFactory(
            new AbundanceProcessesFactory(getInputFolder().path("abundance"), getSpeciesCodesSupplier())
        );
        setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
        setFishingStrategyFactory(new PurseSeinerAbundanceFishingStrategyFactory());
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory());
    }

    /**
     * Just runs the scenario for a year.
     */
    public static void main(final String[] args) {
        final FishState fishState = new FishState();
        final Scenario scenario = new EpoAbundanceScenario();

        try {
            final File scenarioFile =
                Paths.get(
                    System.getProperty("user.home"),
                    "workspace", "tuna", "calibration", "results",
                    "cenv0729", "2022-06-01_18.14.02_global_calibration",
                    "calibrated_scenario.yaml"
                ).toFile();

            //addCsvLogger(Level.DEBUG, "potential_actions", "action,initial,modulated,weighted");
            new FishYAML().dump(scenario, new FileWriter(scenarioFile));
            fishState.setScenario(scenario);
            fishState.start();
            while (fishState.getStep() < 365) {
                System.out.println("Step: " + fishState.getStep());
                fishState.schedule.step(fishState);
            }
            System.out.println("Done.");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @Override
    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    @Override
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

}
