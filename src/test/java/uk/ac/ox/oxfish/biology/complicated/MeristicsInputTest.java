package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;

public class MeristicsInputTest {


    @Test
    public void readsYelloweyeCorrectly() throws Exception {

        FishYAML yaml = new FishYAML();

        MeristicsInput meristicsInput = yaml.loadAs("KParameterFemale: 0.047\n" +
                                                            "KParameterMale: 0.047\n" +
                                                            "addRelativeFecundityToSpawningBiomass: true\n" +
                                                            "ageOld: 70\n" +
                                                            "fecundityIntercept: 137900.0\n" +
                                                            "fecunditySlope: 36500.0\n" +
                                                            "maturityInflection: 38.78\n" +
                                                            "maturitySlope: -0.437\n" +
                                                            "maxAge: 100\n" +
                                                            "maxLengthFemale: 62.265\n" +
                                                            "maxLengthMale: 64.594\n" +
                                                            "mortalityParameterMFemale: 0.046\n" +
                                                            "mortalityParameterMMale: 0.045\n" +
                                                            "steepness: 0.44056\n" +
                                                            "virginRecruits: 228149\n" +
                                                            "weightParameterAFemale: 9.77E-6\n" +
                                                            "weightParameterAMale: 1.7E-5\n" +
                                                            "weightParameterBFemale: 3.17\n" +
                                                            "weightParameterBMale: 3.03\n" +
                                                            "youngAgeFemale: 1.0\n" +
                                                            "youngAgeMale: 1.0\n" +
                                                            "youngLengthFemale: 18.717\n" +
                                                            "youngLengthMale: 18.717",
                                                    MeristicsInput.class);

        StockAssessmentCaliforniaMeristics yellowEye = new StockAssessmentCaliforniaMeristics(meristicsInput);
        assertEquals(yellowEye.getLengthFemaleInCm().get(5),26.4837518217,.001);
        assertEquals(yellowEye.getLengthMaleInCm().get(5),26.8991271545,.001);

        assertEquals(yellowEye.getWeightFemaleInKg().get(5),0.3167667645,.001);
        assertEquals(yellowEye.getWeightMaleInKg().get(5),0.365220907,.001);

        assertEquals(yellowEye.getMaturity().get(5),0.0046166415,.0001);
        assertEquals(yellowEye.getRelativeFecundity().get(5),47344.590014727,.001);
        assertEquals(yellowEye.getPhi().get(5),173.6635925757,.001);

        assertEquals(yellowEye.getCumulativeSurvivalFemale().get(5),0.7945336025,.001);
        assertEquals(yellowEye.getCumulativePhi(),8043057.98636817,.01);

    }


}