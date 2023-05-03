package uk.ac.ox.oxfish.maximization;

import java.nio.file.Paths;

/**
 * just a little class for me to run some main(String[] args) files to resume/evaluate
 * simulation runs that crashed
 */
public class TunaCalibratorHelper {

    public static void main(String[] args){

        double[] best = new double[]{-0.175,-8.976, 14.666,-12.253,-15.149,-0.562,-15.000, 2.498, 10.691, 10.155, 11.123, 14.545, 5.289,-5.486, 14.450,-10.683, 12.639,-7.796,-7.644, 0.437, 13.376, 11.686,-6.076, 7.558,-1.402,-12.524, 9.244,-9.006,-14.192,-2.245,-0.347, 10.491, 14.521,-1.605,-15.117, 7.976,-1.995,-2.738,-5.585, 10.815, 7.209, 0.142,-8.730, 12.533, 14.469, 11.422,-8.580, 6.478,-0.833,-11.739,-12.597, 8.794, 9.256, 9.152,-4.318,-15.547};
        TunaCalibrator.evaluateSolutionAndPrintOutErrors(Paths.get("/home/carrknight/code/tuna/tuna/calibration/results/oneboat_duds/carrknight/2022-02-06_11.06.59_local_dudforced_filtering_search/",
                "calibration.yaml"),best);

    }

}
