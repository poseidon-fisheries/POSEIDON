package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.function.Function;

import static org.apache.commons.lang3.ArrayUtils.indexOf;

public class ExogenousFadSetterCSVFactory implements AlgorithmFactory<ExogenousFadSetterFromData> {


    /**
     * by default, data is in tonnes and simulation is in kg. Because we are dealing with squared errors that accumulate
     * it is probably more numerically stable to deal with tonnes rather than kg (at the cost of rounding errors).
     * This is called to take simulated fad biomass and turn it from kg to tonnes for comparison purposes
     */
    private final static Function<Double, Double> DEFAULT_SIMULATED_TO_DATA_SCALER = simulatedBiomass ->
        simulatedBiomass / 1000;


    public boolean isDataInTonnes = true;
    private InputFile setsFile; // = "./inputs/tests/fad_dummmy_sets.csv";
    private DoubleParameter neighborhoodSearchSize = new FixedDoubleParameter(0);
    private DoubleParameter missingFadError = new FixedDoubleParameter(ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR);
    private boolean keepLog = false;

    /**
     * Empty constructor for YAML loading
     */
    public ExogenousFadSetterCSVFactory() {
    }

    public ExogenousFadSetterCSVFactory(final InputFile setsFile, final boolean isDataInTonnes) {
        this.setsFile = setsFile;
        this.isDataInTonnes = isDataInTonnes;
    }

    @Override
    public ExogenousFadSetterFromData apply(final FishState state) {
        //read the file now (don't delay the error from not having the file ready)
        final CSVReader reader;
        try {
            reader = new CSVReader(
                new FileReader(
                    setsFile.get().toFile()
                )
            );
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read " + setsFile);
        }
        //read header and lowercase it
        final Iterator<String[]> linesInCSV = reader.iterator();
        final String[] header = Arrays.stream(linesInCSV.next()).
            map(s -> s.toLowerCase(Locale.ROOT).trim()).
            toArray(String[]::new);
        final int dayColumn = indexOf(header, "day");
        final int xColumn = indexOf(header, "x");
        final int yColumn = indexOf(header, "y");
        //find each species column
        final int[] speciesColumn = new int[state.getSpecies().size()];
        for (final Species species : state.getSpecies()) {

            speciesColumn[species.getIndex()] = indexOf(
                header,
                species.getName().toLowerCase(Locale.ROOT).trim()
            );
            Preconditions.checkState(
                speciesColumn[species.getIndex()] >= 0,
                "Missing column for " + species
            );
        }
        final HashMap<Integer, List<FadSetObservation>> dayToCoordinatesMap = new HashMap<>();
        while (linesInCSV.hasNext()) {
            final String[] line = linesInCSV.next();
            final int day = Integer.parseInt(line[dayColumn]);
            //if first FAD of the day, create container
            //start with the biomass to put in
            final double[] biomassLanded = new double[speciesColumn.length];
            for (int i = 0; i < biomassLanded.length; i++) {
                biomassLanded[i] = Double.parseDouble(line[speciesColumn[i]]);
            }
            dayToCoordinatesMap.computeIfAbsent(day, integer -> new LinkedList<>()).
                //then add
                    add(new FadSetObservation(
                    new Coordinate(
                        Double.parseDouble(line[xColumn]),
                        Double.parseDouble(line[yColumn])
                    ),
                    biomassLanded,
                    day

                ));
        }
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


    public InputFile getSetsFile() {
        return setsFile;
    }

    public void setSetsFile(final InputFile setsFile) {
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
