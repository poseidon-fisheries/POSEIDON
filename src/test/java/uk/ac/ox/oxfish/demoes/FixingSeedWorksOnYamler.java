package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.YamlMain;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/16/16.
 */
public class FixingSeedWorksOnYamler {


    @Test
    public void fixingSeedWorksOnYamler() throws Exception
    {

        Path firstInputPath = Paths.get("inputs", "tests", "replicate.yaml");
        Path secondInputPath = Paths.get("inputs", "tests", "replicate2.yaml");
        String firstInput = String.join("\n", Files.readAllLines(firstInputPath));
        String secondInput = String.join("\n", Files.readAllLines(secondInputPath));
        assertEquals(firstInput,secondInput);

        YamlMain.main(new String[]{firstInputPath.toString(),"--seed","567","--years","3"});
        YamlMain.main(new String[]{secondInputPath.toString(),"--seed","567","--years","3"});

        String firstOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate/result.yaml")));
        String secondOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate2/result.yaml")));
        assertEquals(firstOutput,secondOutput);

        FishStateUtilities.deleteRecursively(Paths.get("output","replicate").toFile());
        FishStateUtilities.deleteRecursively(Paths.get("output","replicate2").toFile());
        File[] filesInOutputFolder = Paths.get("output").toFile().listFiles();
        if(filesInOutputFolder == null || filesInOutputFolder.length==0)
            Paths.get("output").toFile().delete();


    }
}
