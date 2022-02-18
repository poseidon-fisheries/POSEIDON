package uk.ac.ox.oxfish.geography.fads;

import com.opencsv.CSVReader;
import org.metawidget.util.ArrayUtils;
import sim.util.Double2D;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

/**
 * reads csv file, with column "day" for what day each fad gets dropped
 * x,y for the coordinates (simulated coordinates, not grid coordinates)
 */
public class ExogenousFadMakerFactoryCSV implements AlgorithmFactory<AdditionalStartable> {

    private String pathToFile = "./inputs/tests/fad_dummy_deploy.csv";


    private FadInitializerFactory fadInitializerFactory = new BiomassFadInitializerFactory("Species 0");

    @Override
    public AdditionalStartable apply(FishState state) {
        //read the file now (don't delay the error from not having the file ready)
        CSVReader reader = null;
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

        final HashMap<Integer, Collection<Double2D>> dayToCoordinatesMap = new HashMap<>();
        while(linesInCSV.hasNext()){
            String[] line = linesInCSV.next();
            int day = Integer.parseInt(line[dayColumn]);
            //if first FAD of the day, create container
            dayToCoordinatesMap.computeIfAbsent(day, integer -> new LinkedList<>()).
                    //then add
                    add(new Double2D(
                            Double.parseDouble(line[xColumn]),
                            Double.parseDouble(line[yColumn])
                    ));
        }


        return new AdditionalStartable() {
            @Override
            public void start(FishState model) {
                ExogenousFadMaker maker = new ExogenousFadMaker(
                        (FadInitializer)fadInitializerFactory.apply(model),
                        dayToCoordinatesMap

                );
                maker.start(model);


            }
        };



    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public FadInitializerFactory getFadInitializer() {
        return fadInitializerFactory;
    }

    public void setFadInitializer(FadInitializerFactory fadInitializer) {
        this.fadInitializerFactory = fadInitializer;
    }
}
