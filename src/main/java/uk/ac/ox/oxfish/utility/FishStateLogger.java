package uk.ac.ox.oxfish.utility;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Just a logger formatter that prints out as date the model date rather than the clock time
 * Created by carrknight on 6/30/15.
 */
public class FishStateLogger extends Log.Logger{

    private final FileWriter writer;

    private final FishState model;

    public FishStateLogger(FishState model, Path pathToFile) throws IOException {
        this.model = model;
        writer = new FileWriter(pathToFile.toFile());
    }

    @Override
    public void log(int level, String category, String message, Throwable ex) {
        StringBuilder builder = new StringBuilder(256);
        builder.append(model.timeString());
        builder.append(',');
        builder.append(level);
        builder.append(',');
        builder.append(category);
        builder.append(",");
        builder.append(message);


        System.out.println(builder);
        try {
            writer.write(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
