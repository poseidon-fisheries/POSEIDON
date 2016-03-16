package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.YamlMain;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/16/16.
 */
public class FixingSeedWorksOnYamler {


    @Test
    public void fixingSeedWorksOnYamler() throws Exception
    {

        String firstInput = String.join("\n", Files.readAllLines(Paths.get("inputs/replicate.yaml")));
        String secondInput = String.join("\n", Files.readAllLines(Paths.get("inputs/replicate2.yaml")));
        assertEquals(firstInput,secondInput);

        YamlMain.main(new String[]{"inputs/replicate.yaml","567"});
        YamlMain.main(new String[]{"inputs/replicate2.yaml","567"});

        String firstOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate/result.yaml")));
        String secondOutput = String.join("\n", Files.readAllLines(Paths.get("output/replicate2/result.yaml")));
        assertEquals(firstOutput,secondOutput);


    }
}
