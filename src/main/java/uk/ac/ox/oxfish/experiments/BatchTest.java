package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.BatchRunner;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by carrknight on 9/24/16.
 */
public class BatchTest {


    public static void main(String[] args) throws IOException {


        BatchRunner runner = new BatchRunner(
                Paths.get("inputs", "first_paper", "fronts.yaml"),
                2,
                Lists.newArrayList(
                        "Average Cash-Flow",
                        "Average Distance From Port"
                ),
                Paths.get("output", "batch"),
                null,
                0l,
                null);

        runner.run();
        runner.run();
        runner.run();

    }

}
