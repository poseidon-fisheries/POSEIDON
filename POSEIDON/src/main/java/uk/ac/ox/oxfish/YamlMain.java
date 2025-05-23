/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.io.Files;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Created by carrknight on 11/18/15.
 */
public class YamlMain {

    @Parameter(names = {"--heatmap"}, description = "year at which point to start gathering tow heatmap data. Negative values turns it off")
    private Integer towHeatmapGatherer = null;
    @Parameter(names = {"--seed", "-s"}, description = "random seed for simulation")
    private Long seed = System.currentTimeMillis();
    @Parameter(names = {"--log", "-l"}, description = "the verbosity level of the logging")
    private String logLevel = Level.INFO.getName();
    @Parameter(names = {"--years", "-t"}, description = "number of years the simulation has to lspiRun")
    private int yearsToRun = 20;
    @Parameter(names = {"--save"}, description = "saves model on file at the end of the simulation")
    private boolean saveOnExit = false;
    @Parameter(names = {"--policy", "-p"}, description = "osmoseWFSPath to policy script file")
    private String policyScript = null;
    @Parameter(names = {"--data"}, description = "gathers additional data for the model")
    private boolean additionalData = false;

    public static void main(final String[] args) throws IOException {

        /**
         * the first argument is always the scenario file
         */
        final Path inputFile = Paths.get(args[0]);
        final String simulationName = Files.getNameWithoutExtension(inputFile.getFileName().toString());

        final YamlMain main = new YamlMain();
        if (args.length > 1) {
            //if there are multiple parameters, read them up!
            final JCommander jCommander = new JCommander(main);
            jCommander.parse(Arrays.copyOfRange(args, 1, args.length));
        }
        FishStateUtilities.run(simulationName, inputFile, Paths.get("output", simulationName), main.seed, main.logLevel,
            main.additionalData, main.policyScript,
            main.yearsToRun,
            main.towHeatmapGatherer, null, null, null, null
        );


    }


    /**
     * Getter for property 'seed'.
     *
     * @return Value for property 'seed'.
     */
    public Long getSeed() {
        return seed;
    }

    /**
     * Setter for property 'seed'.
     *
     * @param seed Value to set for property 'seed'.
     */
    public void setSeed(final Long seed) {
        this.seed = seed;
    }

    /**
     * Getter for property 'logLevel'.
     *
     * @return Value for property 'logLevel'.
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Setter for property 'logLevel'.
     *
     * @param logLevel Value to set for property 'logLevel'.
     */
    public void setLogLevel(final String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Getter for property 'yearsToRun'.
     *
     * @return Value for property 'yearsToRun'.
     */
    public int getYearsToRun() {
        return yearsToRun;
    }

    /**
     * Setter for property 'yearsToRun'.
     *
     * @param yearsToRun Value to set for property 'yearsToRun'.
     */
    public void setYearsToRun(final int yearsToRun) {
        this.yearsToRun = yearsToRun;
    }

    /**
     * Getter for property 'saveOnExit'.
     *
     * @return Value for property 'saveOnExit'.
     */
    public boolean isSaveOnExit() {
        return saveOnExit;
    }

    /**
     * Setter for property 'saveOnExit'.
     *
     * @param saveOnExit Value to set for property 'saveOnExit'.
     */
    public void setSaveOnExit(final boolean saveOnExit) {
        this.saveOnExit = saveOnExit;
    }

    /**
     * Getter for property 'policyScript'.
     *
     * @return Value for property 'policyScript'.
     */
    public String getPolicyScript() {
        return policyScript;
    }

    /**
     * Setter for property 'policyScript'.
     *
     * @param policyScript Value to set for property 'policyScript'.
     */
    public void setPolicyScript(final String policyScript) {
        this.policyScript = policyScript;
    }

    /**
     * Getter for property 'additionalData'.
     *
     * @return Value for property 'additionalData'.
     */
    public boolean isAdditionalData() {
        return additionalData;
    }

    /**
     * Setter for property 'additionalData'.
     *
     * @param additionalData Value to set for property 'additionalData'.
     */
    public void setAdditionalData(final boolean additionalData) {
        this.additionalData = additionalData;
    }
}
