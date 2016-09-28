package uk.ac.ox.oxfish.experiments;

import com.google.common.io.PatternFilenameFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by carrknight on 9/12/16.
 */
public class RenameJob {

    public static void main(String[] args){

        Path mainDirectory = Paths.get("runs", "social_tuning");

        File[] files = mainDirectory.toFile().listFiles(new PatternFilenameFilter(".+plan.csv"));
        System.out.println(Arrays.toString(files));
        for(File file : files)
        {
            System.out.println(file.getName());
            String newName = file.getName().replace("plan", "");
            String[] components = newName.split("_");
            assert components.length==2;
            newName = components[0]+"-plan_" + components[1];
            Path newFile = mainDirectory.resolve(newName);
            System.out.println(newFile.getFileName());
            file.renameTo(newFile.toFile());
        }


    }
}
