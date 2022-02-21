package uk.ac.ox.oxfish.geography.fads;

import com.google.common.base.Preconditions;
import com.opencsv.CSVReader;
import com.vividsolutions.jts.geom.Coordinate;
import org.metawidget.util.ArrayUtils;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class ExogenousFadSetterCSVFactory implements AlgorithmFactory<ExogenousFadSetterFromData> {


    /**
     * by default, data is in tonnes and simulation is in kg. Because we are dealing with squared errors that accumulate
     * it is probably more numerically stable to deal with tonnes rather than kg (at the cost of rounding errors).
     * This is called to take simulated fad biomass and turn it from kg to tonnes for comparison purposes
     */
    private final static Function<Double,Double> DEFAULT_SIMULATED_TO_DATA_SCALER = simulatedBiomass ->
            simulatedBiomass/1000;


    public boolean isDataInTonnes = true;

    private String pathToFile = "./inputs/tests/fad_dummmy_sets.csv";


    @Override
    public ExogenousFadSetterFromData apply(FishState state) {
        //read the file now (don't delay the error from not having the file ready)
        CSVReader reader;
        try {
            reader = new CSVReader(
                    new FileReader(
                            Paths.get(pathToFile).toFile()
                    )
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read " + pathToFile);
        }
        //read header and lowercase it
        Iterator<String[]> linesInCSV = reader.iterator();
        String[] header = Arrays.stream(linesInCSV.next()).
                map(s -> s.toLowerCase(Locale.ROOT).trim()).
                toArray(String[]::new);
        final int dayColumn = ArrayUtils.indexOf(header,"day");
        final int xColumn = ArrayUtils.indexOf(header,"x");
        final int yColumn = ArrayUtils.indexOf(header,"y");
        //find each species column
        final int[] speciesColumn = new int[state.getSpecies().size()];
        for (Species species : state.getSpecies()) {

            speciesColumn[species.getIndex()] = ArrayUtils.indexOf(header,
                    species.getName().toLowerCase(Locale.ROOT).trim());
            Preconditions.checkState(speciesColumn[species.getIndex()]>=0,
                    "Missing column for "+species);
        }
        final HashMap<Integer, List<FadSetObservation>> dayToCoordinatesMap = new HashMap<>();
        while(linesInCSV.hasNext()){
            String[] line = linesInCSV.next();
            int day = Integer.parseInt(line[dayColumn]);
            //if first FAD of the day, create container
            //start with the biomass to put in
            double[] biomassLanded = new double[speciesColumn.length];
            for (int i = 0; i < biomassLanded.length; i++) {
                biomassLanded[i] = Double.parseDouble(line[speciesColumn[i]]);
            }
            dayToCoordinatesMap.computeIfAbsent(day, integer -> new LinkedList<>()).
                    //then add
                            add(new FadSetObservation(
                                    new Coordinate(
                            Double.parseDouble(line[xColumn]),
                            Double.parseDouble(line[yColumn])),
                            biomassLanded,
                            day

                    ));
        }
        ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dayToCoordinatesMap);
        if(isDataInTonnes)
            setter.setSimulatedToDataScaler(DEFAULT_SIMULATED_TO_DATA_SCALER);
        return setter;

    }


    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public boolean isDataInTonnes() {
        return isDataInTonnes;
    }

    public void setDataInTonnes(boolean dataInTonnes) {
        isDataInTonnes = dataInTonnes;
    }
}
