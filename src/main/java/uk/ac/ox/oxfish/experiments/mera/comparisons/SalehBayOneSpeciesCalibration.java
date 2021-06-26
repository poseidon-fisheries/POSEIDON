package uk.ac.ox.oxfish.experiments.mera.comparisons;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SalehBayOneSpeciesCalibration {


    public static final int SCENARIOS_TO_RUN = 100;

    static Path MAIN_DIRECTORY = Paths.get("docs/mera_hub/diding");

    public static void main(String[] args) throws IOException {
        //calibration
        for (int hotstart = 1; hotstart < SCENARIOS_TO_RUN; hotstart++) {
            MeraFakeOMHotstarts.calibrate(MAIN_DIRECTORY.resolve("hotstarts_three").
                    resolve(String.valueOf(hotstart)).resolve("optimization.yaml"), 50);
        }
    }


}
