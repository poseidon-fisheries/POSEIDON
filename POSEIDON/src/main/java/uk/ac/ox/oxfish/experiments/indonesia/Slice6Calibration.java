package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.biology.complicated.factory.HockeyStickRecruitmentFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.maximization.GenericOptimization;
import uk.ac.ox.oxfish.maximization.generic.FixedDataLastStepTarget;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.plugins.CatchAtBinFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class Slice6Calibration {



//    private static final List<AlgorithmFactory<? extends AdditionalStartable>> pluginsToAddToCalibratedScenario =
//            new LinkedList<>();
//    static {
//
//        SPRAgentBuilder malabaricus = new SPRAgentBuilder();
//        malabaricus.setAssumedKParameter(new FixedDoubleParameter(0.4438437));
//        malabaricus.setAssumedLengthAtMaturity(new FixedDoubleParameter(50.0));
//        malabaricus.setAssumedLengthBinCm(new FixedDoubleParameter(5.0));
//        malabaricus.setAssumedLinf(new FixedDoubleParameter(86.0));
//        malabaricus.setAssumedNaturalMortality(new FixedDoubleParameter(0.3775984));
//        malabaricus.setAssumedVarA(new FixedDoubleParameter(0.00853));
//        malabaricus.setAssumedVarB(new FixedDoubleParameter(3.137));
//        malabaricus.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));
//        malabaricus.setSimulatedMaxAge(new FixedDoubleParameter(100));
//        malabaricus.setSimulatedVirginRecruits(new FixedDoubleParameter(1000));
//        malabaricus.setSpeciesName("Lutjanus malabaricus");
//        malabaricus.setSurveyTag("100_malabaricus");
//        pluginsToAddToCalibratedScenario.add(malabaricus);
//
//
//        SPRAgentBuilder erythropterus = new SPRAgentBuilder();
//        erythropterus.setAssumedKParameter(new FixedDoubleParameter(.5508334));
//        erythropterus.setAssumedLengthAtMaturity(new FixedDoubleParameter(37));
//        erythropterus.setAssumedLengthBinCm(new FixedDoubleParameter(5.0));
//        erythropterus.setAssumedLinf(new FixedDoubleParameter(63.0));
//        erythropterus.setAssumedNaturalMortality(new FixedDoubleParameter(0.4721429));
//        erythropterus.setAssumedVarA(new FixedDoubleParameter(0.0244));
//        erythropterus.setAssumedVarB(new FixedDoubleParameter(2.87));
//        erythropterus.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));
//        erythropterus.setSimulatedMaxAge(new FixedDoubleParameter(100));
//        erythropterus.setSimulatedVirginRecruits(new FixedDoubleParameter(1000));
//        erythropterus.setSpeciesName("Lutjanus erythropterus");
//        erythropterus.setSurveyTag("100_erythropterus");
//        pluginsToAddToCalibratedScenario.add(erythropterus);
//
//        SPRAgentBuilder multidens = new SPRAgentBuilder();
//        multidens.setAssumedKParameter(new FixedDoubleParameter(0.4438437));
//        multidens.setAssumedLengthAtMaturity(new FixedDoubleParameter(50.0));
//        multidens.setAssumedLengthBinCm(new FixedDoubleParameter(5.0));
//        multidens.setAssumedLinf(new FixedDoubleParameter(86));
//        multidens.setAssumedNaturalMortality(new FixedDoubleParameter(0.3775984));
//        multidens.setAssumedVarA(new FixedDoubleParameter(0.02));
//        multidens.setAssumedVarB(new FixedDoubleParameter(2.944));
//        multidens.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));
//        multidens.setSimulatedMaxAge(new FixedDoubleParameter(100));
//        multidens.setSimulatedVirginRecruits(new FixedDoubleParameter(1000));
//        multidens.setSpeciesName("Pristipomoides multidens");
//        multidens.setSurveyTag("100_multidens");
//        pluginsToAddToCalibratedScenario.add(multidens);
//
//
//        SPRAgentBuilder areolatus = new SPRAgentBuilder();
//        areolatus.setAssumedKParameter(new FixedDoubleParameter(0.3300512));
//        areolatus.setAssumedLengthAtMaturity(new FixedDoubleParameter(21.0));
//        areolatus.setAssumedLengthBinCm(new FixedDoubleParameter(5.0));
//        areolatus.setAssumedLinf(new FixedDoubleParameter(45));
//        areolatus.setAssumedNaturalMortality(new FixedDoubleParameter(0.6011646));
//        areolatus.setAssumedVarA(new FixedDoubleParameter(0.01142));
//        areolatus.setAssumedVarB(new FixedDoubleParameter(3.048));
//        areolatus.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));
//        areolatus.setSimulatedMaxAge(new FixedDoubleParameter(100));
//        areolatus.setSimulatedVirginRecruits(new FixedDoubleParameter(1000));
//        areolatus.setSpeciesName("Epinephelus areolatus");
//        areolatus.setSurveyTag("100_areolatus");
//        pluginsToAddToCalibratedScenario.add(areolatus);
//
//
//
//
//    }


    private static final List<String> additionalPlugins = Lists.newArrayList(
            "SPR Oracle:\n" +
                    "    dayOfMeasurement: 365\n" +
                    "    lengthAtMaturity: 50.0\n" +
                    "    virginSSB: 43871265\n" +
                    "    speciesName: Lutjanus malabaricus",
            "Fishing Mortality Agent:\n" +
                    "    selexParameter1: '28.4833678576524'\n" +
                    "    selexParameter2: '5.9742458342377'\n" +
                    "    selectivityRounding: no\n" +
                    "    speciesName: Lutjanus malabaricus",
            "Fishing Mortality Agent:\n" +
                    "    selexParameter1: '28.4833678576524'\n" +
                    "    selexParameter2: '5.9742458342377'\n" +
                    "    selectivityRounding: no\n" +
                    "    speciesName: Pristipomoides multidens",
            "Fishing Mortality Agent:\n" +
                    "    selexParameter1: '28.4833678576524'\n" +
                    "    selexParameter2: '5.9742458342377'\n" +
                    "    selectivityRounding: no\n" +
                    "    speciesName: Lutjanus erythropterus",
            "SPR Oracle:\n" +
                    "    dayOfMeasurement: 365\n" +
                    "    lengthAtMaturity: 50.0\n" +
                    "    virginSSB: 13957297\n" +
                    "    speciesName: Pristipomoides multidens",
            "SPR Oracle:\n" +
                    "    dayOfMeasurement: 365\n" +
                    "    lengthAtMaturity: 37.0\n" +
                    "    virginSSB: 7815138\n" +
                    "    speciesName: Lutjanus erythropterus",
            "SPR Oracle:\n" +
                    "    dayOfMeasurement: 365\n" +
                    "    lengthAtMaturity: 21.0\n" +
                    "    virginSSB: 9846038.8771707\n" +
                    "    speciesName: Epinephelus areolatus",
            "SPR Agent:\n" +
                    "    assumedKParameter: '0.4438437'\n" +
                    "    assumedLengthAtMaturity: '50.0'\n" +
                    "    assumedLengthBinCm: '5.0'\n" +
                    "    assumedLinf: '86.0'\n" +
                    "    assumedNaturalMortality: '0.3775984'\n" +
                    "    assumedVarA: '0.00853'\n" +
                    "    assumedVarB: '3.137'\n" +
                    "    probabilityOfSamplingEachBoat: '1.0'\n" +
                    "    simulatedMaxAge: '100.0'\n" +
                    "    simulatedVirginRecruits: '1000.0'\n" +
                    "    speciesName: Lutjanus malabaricus\n" +
                    "    surveyTag: 100_malabaricus",
            "SPR Agent:\n" +
                    "    assumedKParameter: '0.5508334'\n" +
                    "    assumedLengthAtMaturity: '37.0'\n" +
                    "    assumedLengthBinCm: '5.0'\n" +
                    "    assumedLinf: '63.0'\n" +
                    "    assumedNaturalMortality: '0.4721429'\n" +
                    "    assumedVarA: '0.0244'\n" +
                    "    assumedVarB: '2.87'\n" +
                    "    probabilityOfSamplingEachBoat: '1.0'\n" +
                    "    simulatedMaxAge: '100.0'\n" +
                    "    simulatedVirginRecruits: '1000.0'\n" +
                    "    speciesName: Lutjanus erythropterus\n" +
                    "    surveyTag: 100_erythropterus",
            "SPR Agent:\n" +
                    "    assumedKParameter: '0.4438437'\n" +
                    "    assumedLengthAtMaturity: '50.0'\n" +
                    "    assumedLengthBinCm: '5.0'\n" +
                    "    assumedLinf: '86.0'\n" +
                    "    assumedNaturalMortality: '0.3775984'\n" +
                    "    assumedVarA: '0.02'\n" +
                    "    assumedVarB: '2.944'\n" +
                    "    probabilityOfSamplingEachBoat: '1.0'\n" +
                    "    simulatedMaxAge: '100.0'\n" +
                    "    simulatedVirginRecruits: '1000.0'\n" +
                    "    speciesName: Pristipomoides multidens\n" +
                    "    surveyTag: 100_multidens",
            "SPR Agent:\n" +
                    "    assumedKParameter: '0.3300512'\n" +
                    "    assumedLengthAtMaturity: '21.0'\n" +
                    "    assumedLengthBinCm: '5.0'\n" +
                    "    assumedLinf: '45.0'\n" +
                    "    assumedNaturalMortality: '0.6011646'\n" +
                    "    assumedVarA: '0.01142'\n" +
                    "    assumedVarB: '3.048'\n" +
                    "    probabilityOfSamplingEachBoat: '1.0'\n" +
                    "    simulatedMaxAge: '100.0'\n" +
                    "    simulatedVirginRecruits: '1000.0'\n" +
                    "    speciesName: Epinephelus areolatus\n" +
                    "    surveyTag: 100_areolatus"
    );

    public static final String SWEEP_FOLDER = "sweeps";
    public static Path DIRECTORY = Paths.get("/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice6");
    public static final double[] STEVE_LIME_OPTIMAL_PARAMETERS = new double[]{
            -6.629, -4.648, -9.882, -2.911, -3.284, 10.000, -1.829, -8.269, 2.941, -3.988, -6.288, -7.519, -10.000, 7.238, -10.000, -6.805, -4.901, -9.386, 2.470, 4.614
    };
    public static final double[] STEVE_LIME_OPTIMAL_LOCAL_PARAMETERS = new double[]{
            5.888,-2.183,-2.043,-6.014, 3.229, 6.774,-6.638, 2.246,-0.465, 5.920, 3.338,-0.546,-5.124, 1.730, 0.867,-5.435, 0.050,-2.330, 0.727,-5.152
    };

    public static final double[] STEVE_TROPFISH_OPTIMAL_PARAMETERS = new double[]{
            5.277,-0.540, 4.601, 1.717,-0.456, 10.000, 8.625,-7.547, 7.348, 3.318,-6.007,-1.607, 4.466, 0.894, 1.042,-3.342, 4.340, 7.971, 7.516,-1.232
    };
    public static final double[] STEVE_TROPFISH_LOCAL_OPTIMAL_PARAMETERS = new double[]{
            6.429, 5.755,-9.223,-5.932,-2.952, 3.022,-7.642,-10.000, 8.081, 8.710,-9.087, 4.748, 4.888, 2.895, 6.431, 7.175,-6.354, 3.021,-1.484, 9.445
    };

    public static final double[] MONTHLY_LIME_OPTIMAL_PARAMETERS = new double[]{
            8.946, 10.000,-2.914, 8.146,-1.634, 10.000, 5.810, 6.324,-6.252, 7.953,-7.386,-7.648,-7.094,-8.340, 0.155, 0.302, 6.034, 0.216, 3.486, 9.676
    };
    public static final double[] MONTHLY_LIME_LOCAL_OPTIMAL_PARAMETERS = new double[]{
            1.352, 8.034, 2.733,-2.958,-0.425, 6.255,-0.334, 3.502,-2.122, 5.870,-3.211,-0.625, 1.152, 4.244, 1.519,-0.540, 1.118, 2.364, 6.605, 3.181
    };


    public static final double[] STEVE_TROPFISH_2YR_OPTIMAL_PARAMETERS = new double[]{
            -6.776,-5.786,-10.000,-4.141,-1.316, 6.773, 1.075,-9.863, 9.288,-4.519, 3.554,-6.341,-9.528, 4.016, 2.727,-3.836,-2.696,-10.000, 2.857,-10.000
    };

    public static final double[] STEVE_TROPFISH_3YR_OPTIMAL_PARAMETERS = new double[]{
            -9.059,-8.210, 6.768, 8.080, 8.390,-1.773, 8.048,-7.891, 9.976,-0.018,-0.137,-10.000, 6.631, 2.561,-4.586,-0.031,-10.000,-9.881,-10.000,-8.771

    };

    public static final double[] LIME_MONTHLY_IMPLIED_2YR_OPTIMAL_PARAMETERS = new double[]{
            -7.916,-8.419,-9.450,-3.011,-2.842, 10.000,-10.000,-5.293,-10.000, 8.950, 1.558,-8.162, 1.657,-1.321,-6.061,-9.018,-8.796,-4.117, 2.576,-5.997
    };

    public static final double[] STEVE_TROPFISH_SR_OPTIMAL_PARAMETERS = new double[]{
            -2.563,-3.677,-10.000,-10.000,-4.524, 2.777, 5.553,-6.567,-5.051,-6.634,-6.678,-10.000,-7.340, 6.825,-6.893,-10.000,-4.533,-10.000, 0.550, 10.000
    };

    public static final double[] TROPFISH_TL_2YR_OPTIMAL_PARAMETERS = new double[]{
            -1.108,-7.843,-7.288, 2.988,-0.376,-4.638,-10.000,-3.300, 4.950, 3.011,-5.885,-2.064, 0.936, 4.905, 9.418, 10.000,-8.976,-8.513, 1.152, 10.000    };

    public static final double[] MONTHLY_CMSY_2YR_LIME_OPTIMAL_PARAMETERS = new double[]{
         //   -8.170, 3.979, 1.344, 4.002,-3.678, 10.000,-1.119, 5.176,-8.503, 0.405,-3.106,-7.122,-0.682, 9.191,-0.105,-8.848, 5.687,-4.989, 9.308, 4.335
            -8.352, 5.400,-4.518,-0.534,-2.782, 9.563, 4.350, 1.451,-7.475, 2.109, 7.264,-10.000,-10.000, 2.914,-0.683,-8.505, 4.915, 3.141,-0.808, 4.917
    };
    public static final double[] MONTHLY_CMSY_3YR_LIME_OPTIMAL_PARAMETERS = new double[]{
            -8.194, 9.476,-2.253,-6.935,-5.158, 3.249, 2.115, 0.332, 8.257,-3.599, 3.965, 8.621, 6.010, 3.344,-10.000,-9.037, 10.000,-0.417,-9.684, 3.634
    };

    public static final double[] TROPFISH_TL_2YR_LOCAL_OPTIMAL_SQUARE = new double[]{
            -7.253, 2.366,-2.280,-3.153,-2.663, 10.000, 10.000,-7.961, 2.422, 10.000, 9.306,-8.707, 1.861,-7.053, 4.577,-4.178, 1.418,-3.409,-6.668, 6.566
    };
    public static final double[] TROPFISH_TL_2YR_OPTIMAL_SQUARE = new double[]{
            -6.528,-7.655, 5.369, 8.138, 1.067, 10.000,-10.000,-8.403, 8.017, 4.227,-8.387,-5.921, 6.268,-9.836, 6.187,-4.136,-8.409,-10.000, 1.574, 9.737
    };



    public static void main(String[] args) throws IOException {

        /////////////////////////////////////////////////////////

        // buildDumpAndRun("steve_lime_calibrationproblem.yaml", "steve_lime_calibrated.yaml", STEVE_LIME_OPTIMAL_PARAMETERS);
//        buildLocalCalibrationProblem("steve_lime_calibrationproblem.yaml",
//                STEVE_LIME_OPTIMAL_PARAMETERS,
//                "steve_lime_local_calibrationproblem.yaml",
//                .2d);
//        buildDumpAndRun("steve_TropFishR_calibrationproblem.yaml",
//                "steve_TropFishR_calibrated.yaml",
//                STEVE_TROPFISH_OPTIMAL_PARAMETERS);


        ////////////////////////////////////////////////////////////////////
//                buildDumpAndRun("steve_local_TropFishR_calibrationproblem.yaml",
//                "steve_TropFishR_calibrated.yaml",
//                                STEVE_TROPFISH_LOCAL_OPTIMAL_PARAMETERS);

//        buildVariants("steve_local_TropFishR_calibrationproblem.yaml",
//                "steve_tropfish",STEVE_TROPFISH_LOCAL_OPTIMAL_PARAMETERS);

//        GenericOptimization.buildLocalCalibrationProblem(
//                DIRECTORY.resolve("calibration").resolve("steve_TropFishR_calibrationproblem.yaml"),
//                STEVE_TROPFISH_OPTIMAL_PARAMETERS,
//                "steve_local_TropFishR_calibrationproblem.yaml",
//                .2d);
//
//
//        buildVariants("steve_lime_local_calibrationproblem.yaml",
//                "steve_lime",STEVE_LIME_OPTIMAL_LOCAL_PARAMETERS);


        //////////////////////////////////////////////////////////////////


//         buildDumpAndRun("LIME_monthly_calibrationproblem.yaml",
//                         "LIME_monthly_calibrated.yaml",
//                         MONTHLY_LIME_OPTIMAL_PARAMETERS);
//        GenericOptimization.buildLocalCalibrationProblem(
//                DIRECTORY.resolve("calibration").resolve("LIME_monthly_calibrationproblem.yaml"),
//                MONTHLY_LIME_OPTIMAL_PARAMETERS,
//                "LIME_monthly_LOCAL_calibrationproblem.yaml",
//                .2d);

//        buildVariants("LIME_monthly_LOCAL_calibrationproblem.yaml",
//                      "monthly_lime",MONTHLY_LIME_LOCAL_OPTIMAL_PARAMETERS);
//                 buildDumpAndRun("LIME_monthly_LOCAL_calibrationproblem.yaml",
//                         "LIME_monthly_LOCAL_calibrated.yaml",
//                                 MONTHLY_LIME_LOCAL_OPTIMAL_PARAMETERS);



        ///////////////////////////////


//        buildVariants("steve_lime_specialreset_calibrationproblem.yaml",
//                      "steve_limesr",STEVE_TROPFISH_SR_OPTIMAL_PARAMETERS);
//        buildDumpAndRun("steve_lime_specialreset_calibrationproblem.yaml",
//                        "steve_limesr.yaml",
//                        STEVE_TROPFISH_SR_OPTIMAL_PARAMETERS);




/////////////////////////

//        buildVariants("LIME_monthly3yr_cmsy_calibrationproblem.yaml",
//                      "lime_cmsy_3yr",MONTHLY_CMSY_3YR_LIME_OPTIMAL_PARAMETERS);
//        buildDumpAndRun("LIME_monthly3yr_cmsy_calibrationproblem.yaml",
//                        "lime_cmsy_3yr.yaml",
//                        MONTHLY_CMSY_3YR_LIME_OPTIMAL_PARAMETERS);
/////////////////////////
//        buildVariants("steve_TropFishR3yr_calibrationproblem.yaml",
//                      "tropfish_cmsy_3yr",STEVE_TROPFISH_3YR_OPTIMAL_PARAMETERS);
//        buildDumpAndRun("steve_TropFishR3yr_calibrationproblem.yaml",
//                        "tropfish_cmsy.yaml",
//                        STEVE_TROPFISH_3YR_OPTIMAL_PARAMETERS);

///////////////////
        //LIME_monthly2yr_implied_calibrationproblem
//                buildVariants("TropFishR_tl_2yr_calibrationproblem.yaml",
//                      "tropfishR_tl_2yr",TROPFISH_TL_2YR_OPTIMAL_PARAMETERS);
//        buildDumpAndRun("TropFishR_tl_2yr_calibrationproblem.yaml",
//                        "tropfishR_tl_2yr.yaml",
//                        TROPFISH_TL_2YR_OPTIMAL_PARAMETERS);

//                GenericOptimization.buildLocalCalibrationProblem(
//                DIRECTORY.resolve("calibration").resolve("TropFishR_tl_2yr_calibrationproblem.yaml"),
//                TROPFISH_TL_2YR_OPTIMAL_PARAMETERS,
//                "TropFishR_tl_2yr_calibrationproblem_LOCAL.yaml",
//                .2d);


        ///////////////////
        //LIME_monthly2yr_implied_calibrationproblem
//                buildVariants("LIME_monthly2yr_implied_calibrationproblem.yaml",
//                      "lime_implied_monthly_2yr",LIME_MONTHLY_IMPLIED_2YR_OPTIMAL_PARAMETERS);
//        buildDumpAndRun("LIME_monthly2yr_implied_calibrationproblem.yaml",
//                        "lime_implied_monthly_2yr.yaml",
//                        LIME_MONTHLY_IMPLIED_2YR_OPTIMAL_PARAMETERS);



        ////////////////////////////
        // TROPFISH  LOCAL


//        buildDumpAndRun("TropFishR_tl_2yr_onemoretime_LOCAL_square.yaml",
//                        "local1.yaml",
//                        TROPFISH_TL_2YR_LOCAL_OPTIMAL_SQUARE);
//
//        GenericOptimization.buildLocalCalibrationProblem(
//                DIRECTORY.resolve("calibration").resolve("TropFishR_tl_2yr_onemoretime_LOCAL_square.yaml"),
//                TROPFISH_TL_2YR_LOCAL_OPTIMAL_SQUARE,
//                "TropFishR_tl_2yr_calibrationproblem_LOCAL_square_2.yaml",
//                .2d);
//      //  TROPFISH_TL_2YR_LOCAL_SQUARE
//        buildDumpAndRun("TropFishR_tl_2yr_onemoretime_square.yaml",
//                        "square.yaml",
//                        TROPFISH_TL_2YR_OPTIMAL_SQUARE);


        //asymmetric, reset
//        double[] assymmetric = {10.0, 6.946985348355879, -4.070880334062945, -0.39993284033361287, -4.665437254408304, 0.760754252454288, -5.488095597477981, -1.4657944426976162, -3.8307866223813902, -4.960560943421498, 9.349572245609385, 5.024023869450333, -0.630641655846433, 1.468378223691592, -8.831685814118083, 4.83016481516259, 5.847818369682752, -9.05702738458986, -4.804754704104122, 7.48564900937437};
//
//        buildDumpAndRun("TropFishR_tl_reset_calibrationproblem_asymmetric.yaml",
//                        "asym-nospinup.yaml",
//                        assymmetric);
        //asymmetric, no spinup
//        double[] assymmetric2 = {
//                5.132, 6.985, 8.244, 9.007,-10.000,-0.118,-8.022, 8.130, 4.953,-4.059, 0.786, 7.554, 2.744,-3.464, 1.313,-4.138,-9.064,-1.519, 2.867,-1.599};
//
////        buildDumpAndRun("TropFishR_tl_reset_calibrationproblem_asymmetric_noreboot.yaml",
////                        "asym-norest.yaml",
////                        assymmetric2);
//
//        buildVariants("TropFishR_tl_reset_calibrationproblem_asymmetric_noreboot.yaml",
//                      "tropfish-noreset",
//                      assymmetric2);

//        double[] resetAsymm = {4.767,-3.325, 6.289,-2.022,-8.506, 3.109, 7.534, 2.398,-7.871,-3.968,-5.019,-3.817,-7.474,-10.000,-3.866, 2.877,-1.618, 10.000, 7.622, 3.194};
//
//        buildDumpAndRun("LIME_calibrationproblem_asymmetric_1yr_reboot.yaml",
//                        "asym-rest.yaml",
//                        resetAsymm);
//
//        GenericOptimization.buildLocalCalibrationProblem(
//                DIRECTORY.resolve("calibration").resolve("LIME_calibrationproblem_asymmetric_1yr_reboot.yaml"),
//                resetAsymm,
//                "LIME_calibrationproblem_asymmetric_1yr_reboot_LOCAL_square_2.yaml",
//                .2d);


//        double[] resetAsymmLocal = {-6.281, 4.622, 5.169, 4.294,-8.433, 3.298, 1.703, 3.316,-1.013, 3.331,-1.909,-5.479, 6.782,-0.548,-3.606, 1.712, 0.967, 5.470,-1.741, 4.822};


//
//        buildDumpAndRun("LIME_calibrationproblem_asymmetric_1yr_reboot_LOCAL_square_2.yaml",
//                        "asym-rest_loc.yaml",
//                        resetAsymmLocal);
//        buildVariants("LIME_calibrationproblem_asymmetric_1yr_reboot_LOCAL_square_2.yaml",
//                      "lime-reset",
//                      resetAsymmLocal);


//        double[] cmsyTropfishR = {5.950,-5.219,-4.420,-1.055, 4.433, 1.867,-9.522, 3.486,-2.111, 3.560,-4.872,-5.343,-3.452,-2.421, 0.380,-3.107,-3.860, 0.080, 7.842, 3.090};
//
//
////        buildDumpAndRun("TropFishR_tl_2yr_cmsy_square.yaml",
////                        "test.yaml",
////                        cmsyTropfishR);
//
//
//        buildVariants(
//
//                "TropFishR_tl_2yr_cmsy_square.yaml",
//                "cmsy_tropfishR",
//                cmsyTropfishR
//
//        );


//        double[] cmsyTropfishRReset = {-0.038, 2.159, 1.279, 1.814,-3.028, 1.906,-2.961,-4.841,-1.897,-2.837, 4.237,-5.249, 2.952,-2.998,-1.761,-0.631, 0.274, 2.911, 1.653, 1.232};
//                buildDumpAndRun("TropFishR_tl_reset_cmsy_square.yaml",
//                        "test.yaml",
//                        cmsyTropfishRReset);
//
//
//                buildVariants(
//
//                "TropFishR_tl_reset_cmsy_square.yaml",
//                "cmsy_tropfishR_reset",
//                cmsyTropfishRReset
//
//        );
//
//
//                double[] tropfishRNewCMSY = {2.324,-4.218, 1.553, 3.494, 4.637, 5.638,-8.044, 4.339, 2.344, 0.070,-2.449,-2.121, 2.268,-0.438,-3.765,-6.050,-4.285,-3.959, 1.782, 5.956};
//        buildDumpAndRun("TropFishR_tl_2yr_NEWcmsy_square.yaml",
//                        "test.yaml",
//                        tropfishRNewCMSY);
//
//
//        buildVariants(
//
//                "TropFishR_tl_2yr_NEWcmsy_square.yaml",
//                "new_cmsy_tropfishR",
//                tropfishRNewCMSY
//
//        );
//
//        double[] LIME1yrmonthly = {-1.960, 8.301, 2.100,-5.573,-2.853, 8.855, 5.481, 4.415, 3.192, 7.330, 5.670, 0.463,-0.486, 7.699,-5.471, 6.583, 2.854, 6.598, 7.246, 0.527};
//        buildDumpAndRun("LIME_monthly1yr_actualcmsy_calibrationproblem.yaml",
//                        "test.yaml",
//                        LIME1yrmonthly);
//
//
//        buildVariants(
//
//                "LIME_monthly1yr_actualcmsy_calibrationproblem.yaml",
//                "lime_monthly1yr",
//                LIME1yrmonthly
//
//        );
        double[] LIME2yrmonthly = {-7.255, 4.413,-7.207, 3.353,-3.222, 9.547,-9.115, 8.571, 8.844, 0.873,-3.995,-8.569,-4.873,-2.593, 5.986,-4.296, 3.977,-8.123, 6.935, 3.333};
        buildDumpAndRun("LIME_monthly2yr_actualcmsy_calibrationproblem.yaml",
                        "test.yaml",
                        LIME2yrmonthly);


        buildVariants(

                "LIME_monthly2yr_actualcmsy_calibrationproblem.yaml",
                "lime_monthly2yr",
                LIME2yrmonthly

        );

    }

    /**
     * generates the new scenario given the best parameters for it, saves it to file and runs it
     * @param calibrationFileName
     * @param calibratedScenarioFileName
     * @param optimalParameters
     */
    public static void buildDumpAndRun(String calibrationFileName,
                                       String calibratedScenarioFileName,
                                       double[] optimalParameters) throws IOException {


        FishYAML yaml = new FishYAML();
        Path optimizationFile = DIRECTORY.resolve("calibration").resolve(calibrationFileName);
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);



        Scenario scenario = GenericOptimization.buildScenario(optimalParameters, Paths.get(optimization.getScenarioFile()).toFile(), optimization.getParameters());
        Path outputFile = optimizationFile.getParent().resolve(calibratedScenarioFileName);
        yaml.dump(scenario, new FileWriter(outputFile.toFile()));


        FixedDataLastStepTarget.VERBOSE=true;
        optimization.evaluate(optimalParameters);
        FixedDataLastStepTarget.VERBOSE=false;


    }

    public static void buildVariants(String calibrationFileName,
                                     String calibratedScenarioFileName,
                                     double[] optimalParameters) throws IOException {


        FishYAML yaml = new FishYAML();
        Path optimizationFile = DIRECTORY.resolve("calibration").resolve(calibrationFileName);
        GenericOptimization optimization = yaml.loadAs(new FileReader(optimizationFile.toFile()), GenericOptimization.class);

        List<AlgorithmFactory<? extends AdditionalStartable>> plugins = new LinkedList<>();
        for (String additionalPlugin : additionalPlugins) {
            plugins.add(
            yaml.loadAs(additionalPlugin,AlgorithmFactory.class)
            );

        }


        //variants with steepness
        for(double steepness : new double[]{0.6,0.7,0.8}) {

            int printName = (int) (steepness * 10);
            Scenario scenario = GenericOptimization.buildScenario(optimalParameters, Paths.get(optimization.getScenarioFile()).toFile(), optimization.getParameters());
            FlexibleScenario modified = (FlexibleScenario) scenario;
            modified.getPlugins().add(new CatchAtBinFactory());


            for (AlgorithmFactory<? extends AdditionalStartable> plugin : plugins) {
                modified.getPlugins().add(plugin);
            }
            MultipleIndependentSpeciesAbundanceFactory bio = (MultipleIndependentSpeciesAbundanceFactory) modified.getBiologyInitializer();
            for (AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
                ((RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment()).
                        setSteepness(new FixedDoubleParameter(steepness));
            }
            Path outputFile = optimizationFile.getParent().resolve(SWEEP_FOLDER).resolve(calibratedScenarioFileName + "_"+printName+"h.yaml");
            yaml.dump(modified, new FileWriter(outputFile.toFile()));
        }
        //variants with linear recruitment
        Scenario scenario = GenericOptimization.buildScenario(optimalParameters, Paths.get(optimization.getScenarioFile()).toFile(), optimization.getParameters());
        FlexibleScenario modified = (FlexibleScenario) scenario;
        modified.getPlugins().add(new CatchAtBinFactory());
        for (AlgorithmFactory<? extends AdditionalStartable> plugin : plugins) {
            modified.getPlugins().add(plugin);
        }
        MultipleIndependentSpeciesAbundanceFactory bio = (MultipleIndependentSpeciesAbundanceFactory) modified.getBiologyInitializer();
        for(AlgorithmFactory<? extends SingleSpeciesAbundanceInitializer> factory : bio.getFactories()) {
            final RecruitmentBySpawningJackKnifeMaturity oldRecruitment = (RecruitmentBySpawningJackKnifeMaturity) ((SingleSpeciesAbundanceFactory) factory).getRecruitment();


            HockeyStickRecruitmentFactory newRecruitment = new HockeyStickRecruitmentFactory();
            newRecruitment.setHinge(new FixedDoubleParameter(.2));
            newRecruitment.setLengthAtMaturity(new FixedDoubleParameter(oldRecruitment.getLengthAtMaturity()));
            final double r0 = ((FixedDoubleParameter) oldRecruitment.getVirginRecruits()).getFixedValue();
            final double ssb0 = r0* ((FixedDoubleParameter) oldRecruitment.getCumulativePhi()).getFixedValue();
            newRecruitment.setVirginRecruits(new FixedDoubleParameter(r0));

            newRecruitment.setVirginSpawningBiomass(
                    new FixedDoubleParameter(
                            ssb0
                    )
            );

            ((SingleSpeciesAbundanceFactory) factory).setRecruitment(newRecruitment);
        }
        Path outputFile = optimizationFile.getParent().resolve(SWEEP_FOLDER).resolve(calibratedScenarioFileName + "_hs.yaml");
        yaml.dump(modified, new FileWriter(outputFile.toFile()));




    }

}
