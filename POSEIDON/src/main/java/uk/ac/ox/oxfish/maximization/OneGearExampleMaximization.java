/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.maximization;

import eva2.problems.simple.SimpleProblemDouble;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OneGearExampleMaximization extends SimpleProblemDouble {


    private static final long serialVersionUID = 8514838882100771859L;
    private String scenarioFile =
        Paths.get("coe_new.yaml").toString();


    private String summaryFile =
        Paths.get(".").toString();

    private String landingData =
        Paths.get("landings_10.csv").toString();

    private long seed = 0;


    // an input of +10 means this is the catchability
    private double maxCatchability = 0.002;

    //an input of -10 means this catchability
    private double minCatchability = 0.00001;


    private int yearsToRun = 9;

    private int yearsToIgnore = 0;


    private int runsPerSetting = 1;
    private String speciesName = "Peter Snapper";

    @Override
    public double[] evaluate(final double[] x) {
        final FishYAML yaml = new FishYAML();
        final Path scenarioPath = Paths.get(scenarioFile);

        try {
            double error = 0;
            for (int i = 0; i < runsPerSetting; i++) {
                final FlexibleScenario scenario = yaml.loadAs(
                    new FileReader(scenarioPath.toFile()),
                    FlexibleScenario.class
                );

                final double catchability = ((x[0] + 10) / 20) * (maxCatchability - minCatchability);

                for (final FisherDefinition definition : scenario.getFisherDefinitions()) {
                    ((RandomCatchabilityTrawlFactory) definition.getGear()).setMeanCatchabilityFirstSpecies(
                        new FixedDoubleParameter(catchability)
                    );
                }


                final FishState model = new FishState(System.currentTimeMillis());
                model.setScenario(scenario);
                model.start();
                System.out.println("starting run");
                while (model.getYear() < yearsToRun) {
                    model.schedule.step(model);
                }
                model.schedule.step(model);

                error += FishStateUtilities.timeSeriesDistance(
                    model.getYearlyDataSet().getColumn(
                        speciesName + " Landings"
                    ),
                    Paths.get(landingData), 1
                );

            }

            return new double[]{
                error / (double) runsPerSetting};
        } catch (final IOException exception) {
            throw new RuntimeException("failed to read or deal with files!");
        }
    }


    @Override
    public int getProblemDimension() {
        return 1;
    }


    public String getScenarioFile() {
        return scenarioFile;
    }

    public void setScenarioFile(final String scenarioFile) {
        this.scenarioFile = scenarioFile;
    }

    public String getSummaryFile() {
        return summaryFile;
    }

    public void setSummaryFile(final String summaryFile) {
        this.summaryFile = summaryFile;
    }

    public String getLandingData() {
        return landingData;
    }

    public void setLandingData(final String landingData) {
        this.landingData = landingData;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(final long seed) {
        this.seed = seed;
    }

    public double getMaxCatchability() {
        return maxCatchability;
    }

    public void setMaxCatchability(final double maxCatchability) {
        this.maxCatchability = maxCatchability;
    }

    public double getMinCatchability() {
        return minCatchability;
    }

    public void setMinCatchability(final double minCatchability) {
        this.minCatchability = minCatchability;
    }

    public int getYearsToRun() {
        return yearsToRun;
    }

    public void setYearsToRun(final int yearsToRun) {
        this.yearsToRun = yearsToRun;
    }

    public int getYearsToIgnore() {
        return yearsToIgnore;
    }

    public void setYearsToIgnore(final int yearsToIgnore) {
        this.yearsToIgnore = yearsToIgnore;
    }

    public int getRunsPerSetting() {
        return runsPerSetting;
    }

    public void setRunsPerSetting(final int runsPerSetting) {
        this.runsPerSetting = runsPerSetting;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(final String speciesName) {
        this.speciesName = speciesName;
    }
}
