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

package uk.ac.ox.oxfish.geography.fads;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class ExogenousFadSetterCSVFactory implements AlgorithmFactory<ExogenousFadSetterFromData>, Dummyable {

    /**
     * by default, data is in tonnes and simulation is in kg. Because we are dealing with squared errors that accumulate
     * it is probably more numerically stable to deal with tonnes rather than kg (at the cost of rounding errors). This
     * is called to take simulated fad biomass and turn it from kg to tonnes for comparison purposes
     */
    private final static Function<Double, Double> DEFAULT_SIMULATED_TO_DATA_SCALER = simulatedBiomass ->
        simulatedBiomass / 1000;

    public boolean isDataInTonnes = true;
    private InputPath setsFile; // = "./inputs/tests/fad_dummmy_sets.csv";
    private DoubleParameter neighborhoodSearchSize = new FixedDoubleParameter(0);
    private DoubleParameter missingFadError =
        new FixedDoubleParameter(ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR);
    private boolean keepLog = false;

    /**
     * Empty constructor for YAML loading
     */
    public ExogenousFadSetterCSVFactory() {
    }

    public ExogenousFadSetterCSVFactory(
        final InputPath setsFile,
        final boolean isDataInTonnes
    ) {
        this.setsFile = setsFile;
        this.isDataInTonnes = isDataInTonnes;
    }

    @Override
    public ExogenousFadSetterFromData apply(final FishState state) {
        final List<Species> speciesList = state.getBiology().getSpecies();
        final Map<Integer, List<FadSetObservation>> dayToCoordinatesMap =
            recordStream(setsFile.get()).collect(groupingBy(
                r -> r.getInt("day"),
                mapping(
                    r -> new FadSetObservation(
                        new Coordinate(r.getDouble("x"), r.getDouble("y")),
                        speciesList.stream().mapToDouble(s -> r.getDouble(s.getName())).toArray(),
                        r.getInt("day")
                    ),
                    toList()
                )
            ));
        final ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dayToCoordinatesMap);
        if (isDataInTonnes)
            setter.setSimulatedToDataScaler(DEFAULT_SIMULATED_TO_DATA_SCALER);
        final int range = (int) neighborhoodSearchSize.applyAsDouble(state.getRandom());
        setter.setNeighborhoodSearchSize(range);
        setter.setMissingFadError(getMissingFadError().applyAsDouble(state.getRandom()));
        if (keepLog)
            setter.startOrResetLogger(state);
        return setter;
    }

    public DoubleParameter getMissingFadError() {
        return missingFadError;
    }

    public void setMissingFadError(final DoubleParameter missingFadError) {
        this.missingFadError = missingFadError;
    }

    public InputPath getSetsFile() {
        return setsFile;
    }

    public void setSetsFile(final InputPath setsFile) {
        this.setsFile = setsFile;
    }

    public boolean isDataInTonnes() {
        return isDataInTonnes;
    }

    public void setDataInTonnes(final boolean dataInTonnes) {
        isDataInTonnes = dataInTonnes;
    }

    public DoubleParameter getNeighborhoodSearchSize() {
        return neighborhoodSearchSize;
    }

    public void setNeighborhoodSearchSize(final DoubleParameter neighborhoodSearchSize) {
        this.neighborhoodSearchSize = neighborhoodSearchSize;
    }

    public boolean isKeepLog() {
        return keepLog;
    }

    public void setKeepLog(final boolean keepLog) {
        this.keepLog = keepLog;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        setsFile = dummyDataFolder.path("dummy_fad_sets.csv");
    }
}
