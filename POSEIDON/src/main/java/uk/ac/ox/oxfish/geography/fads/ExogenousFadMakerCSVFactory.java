package uk.ac.ox.oxfish.geography.fads;

import com.opencsv.CSVReader;
import sim.util.Double2D;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputFile;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.indexOf;

/**
 * reads csv file, with column "day" for what day each fad gets dropped
 * x,y for the coordinates (simulated coordinates, not grid coordinates)
 */
public class ExogenousFadMakerCSVFactory implements AlgorithmFactory<AdditionalStartable> {

    private InputFile deploymentsFile; // = "./inputs/tests/fad_dummy_deploy.csv";

    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory =
        new BiomassFadInitializerFactory("Species 0");

    /**
     * Empty constructor for YAML initialization
     */
    public ExogenousFadMakerCSVFactory() {
    }


    public ExogenousFadMakerCSVFactory(
        final InputFile deploymentsFile,
        @SuppressWarnings("rawtypes") final FadInitializerFactory fadInitializerFactory
    ) {
        this.deploymentsFile = deploymentsFile;
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @Override
    public AdditionalStartable apply(final FishState state) {
        //read the file now (don't delay the error from not having the file ready)
        final CSVReader reader;
        try {
            reader = new CSVReader(
                new FileReader(
                    deploymentsFile.get().toFile()
                )
            );
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to read " + deploymentsFile);
        }
        //read header and lowercase it
        final Iterator<String[]> linesInCSV = reader.iterator();
        final String[] header = Arrays.stream(linesInCSV.next()).
            map(s -> s.toLowerCase(Locale.ROOT).trim()).
            toArray(String[]::new);
        final int dayColumn = indexOf(header, "day");
        final int xColumn = indexOf(header, "x");
        final int yColumn = indexOf(header, "y");

        final HashMap<Integer, Collection<Double2D>> dayToCoordinatesMap = new HashMap<>();
        while (linesInCSV.hasNext()) {
            final String[] line = linesInCSV.next();
            final int day = Integer.parseInt(line[dayColumn]);
            //if first FAD of the day, create container
            dayToCoordinatesMap.computeIfAbsent(day, integer -> new LinkedList<>()).
                //then add
                    add(new Double2D(
                    Double.parseDouble(line[xColumn]),
                    Double.parseDouble(line[yColumn])
                ));
        }


        return model -> {
            final ExogenousFadMaker maker = new ExogenousFadMaker(
                fadInitializerFactory.apply(model),
                dayToCoordinatesMap

            );
            maker.start(model);
        };
    }

    public InputFile getDeploymentsFile() {
        return deploymentsFile;
    }

    public void setDeploymentsFile(final InputFile deploymentsFile) {
        this.deploymentsFile = deploymentsFile;
    }

    public AlgorithmFactory<? extends FadInitializer> getFadInitializer() {
        return fadInitializerFactory;
    }

    public void setFadInitializer(final AlgorithmFactory<? extends FadInitializer> fadInitializer) {
        this.fadInitializerFactory = fadInitializer;
    }
}
