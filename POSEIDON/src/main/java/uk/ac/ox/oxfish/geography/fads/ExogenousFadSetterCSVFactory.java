package uk.ac.ox.oxfish.geography.fads;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class ExogenousFadSetterCSVFactory implements AlgorithmFactory<ExogenousFadSetterFromData> {


    /**
     * by default, data is in tonnes and simulation is in kg. Because we are dealing with squared errors that accumulate
     * it is probably more numerically stable to deal with tonnes rather than kg (at the cost of rounding errors).
     * This is called to take simulated fad biomass and turn it from kg to tonnes for comparison purposes
     */
    private final static Function<Double, Double> DEFAULT_SIMULATED_TO_DATA_SCALER = simulatedBiomass ->
        simulatedBiomass / 1000;


    public boolean isDataInTonnes = true;
    private InputPath setsFile; // = "./inputs/tests/fad_dummmy_sets.csv";
    private DoubleParameter neighborhoodSearchSize = new FixedDoubleParameter(0);
    private DoubleParameter missingFadError = new FixedDoubleParameter(ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR);
    private boolean keepLog = false;

    /**
     * Empty constructor for YAML loading
     */
    public ExogenousFadSetterCSVFactory() {
    }

    public ExogenousFadSetterCSVFactory(final InputPath setsFile, final boolean isDataInTonnes) {
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
        final int range = neighborhoodSearchSize.apply(state.getRandom()).intValue();
        setter.setNeighborhoodSearchSize(range);
        setter.setMissingFadError(getMissingFadError().apply(state.getRandom()));
        if (keepLog)
            setter.startOrResetLogger(state);
        return setter;
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

    public DoubleParameter getMissingFadError() {
        return missingFadError;
    }

    public void setMissingFadError(final DoubleParameter missingFadError) {
        this.missingFadError = missingFadError;
    }

    public boolean isKeepLog() {
        return keepLog;
    }

    public void setKeepLog(final boolean keepLog) {
        this.keepLog = keepLog;
    }
}
