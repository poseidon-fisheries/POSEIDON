package uk.ac.ox.oxfish.model.regs.policymakers;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SurplusProductionStockAssessmentTest {


    @Test
    public void computesRResults() {

        double[] observedLandings = new double[]{
            1566974.3, 1328929.4, 1248055.3, 1184985.2, 1165341.3, 1142736.6, 1101573.9, 1078426.7, 1072693.6,
            1090177.6, 462732.4, 501729.1, 502527.5, 502286.6, 525844.8, 581686.7, 578290.7, 584485, 596166.7,
            593651.5, 588800.8, 573955.4, 573492.6, 582562.1, 553766.4, 496149.2, 548488.9, 529450.8, 523496.8, 526328.8
        };

        final SurplusProductionResult surplus =
            SurplusProductionStockAssessment.simulateSchaefer(
                11987685,
                0.4033695,
                9.701158e-08,
                observedLandings
            );

        Assert.assertEquals(
            surplus.getCarryingCapacity(),
            11987685,
            .0001
        );
        Assert.assertEquals(
            surplus.getLogisticGrowth(),
            0.4033695,
            .0001
        );
        Assert.assertEquals(
            surplus.getCatchability(),
            9.701158e-08,
            .0001
        );

        Assert.assertEquals(
            surplus.getDepletion()[observedLandings.length - 1],
            0.8733650,
            .0001
        );
        Assert.assertEquals(
            surplus.getCpue()[observedLandings.length - 1],
            1.0145001,
            .0001
        );

    }

    @Test
    public void assess() {

        double[] observedLandings = new double[]{
            1566974.3, 1328929.4, 1248055.3, 1184985.2, 1165341.3, 1142736.6, 1101573.9, 1078426.7, 1072693.6,
            1090177.6, 462732.4, 501729.1, 502527.5, 502286.6, 525844.8, 581686.7, 578290.7, 584485, 596166.7,
            593651.5, 588800.8, 573955.4, 573492.6, 582562.1, 553766.4, 496149.2, 548488.9, 529450.8, 523496.8, 526328.8
        };

        double[] observedCPUE = new double[]{
            1.08745172752714, 0.881891568645702, 0.818444637174724, 0.771701774289279, 0.755293026810786,
            0.739472590561737, 0.708132202581799, 0.692451697335559, 0.689620727175355, 0.699154416346649,
            0.788024998193509, 0.824275709412075, 0.856019045835699, 0.91262719365589, 1.00173333481363,
            1.01366523243771, 1.03569499972739, 1.0621205873725, 1.06958971853562, 1.07365658801976,
            1.065699506414, 1.07523647079321, 1.09502142623632, 1.06885556096705, 1.01146134425654, 1.08749884144951,
            1.07339447166713, 1.07582891227937, 1.08828686189729, 1.05707642248746
        };

        final SurplusProductionResult assessment = SurplusProductionStockAssessment.assess(
            observedLandings,
            observedCPUE,
            new double[]{100000d, 100000000d},
            new double[]{0.4, 0.8},
            new double[]{0, .001}
        );

        //this is a bit random and EVA does better than nelderMead on R so let's just make sure the CPUE error is below 0.2
        double[] simulatedCPUE = assessment.getCpue();
        double sumDistance = 0;
        for (int year = 0; year < simulatedCPUE.length; year++) {

            if (simulatedCPUE[year] < 0) //negative CPUE is unacceptable: we are somewhere shit
                assertTrue(false);
            sumDistance += Math.pow(
                simulatedCPUE[year] - observedCPUE[year]
                , 2);

        }
        System.out.println("SP error: " + sumDistance);
        System.out.println(assessment);
        Assert.assertTrue(sumDistance < .2);


    }
}